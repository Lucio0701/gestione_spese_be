package com.gestionespese.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gestionespese.dto.agent.AgentMessage;
import com.gestionespese.dto.budget.BudgetCreateRequest;
import com.gestionespese.dto.budget.BudgetDto;
import com.gestionespese.dto.budget.BudgetPeriod;
import com.gestionespese.dto.expense.ExpenseCreateRequest;
import com.gestionespese.dto.expense.ExpenseDto;
import com.gestionespese.dto.expense.PagedExpenses;
import com.gestionespese.model.TransactionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final ObjectMapper objectMapper;

    @Value("${bedrock.region:eu-west-1}")
    private String bedrockRegion;

    @Value("${bedrock.model-id:amazon.nova-lite-v1:0}")
    private String modelId;

    public AgentService(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    public String chat(String userEmail, List<AgentMessage> history) {
        try (BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.of(bedrockRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            // Bedrock requires the conversation to start with a user message — skip leading assistant messages
            List<AgentMessage> filtered = new ArrayList<>(history);
            while (!filtered.isEmpty() && "assistant".equals(filtered.get(0).role())) {
                filtered.remove(0);
            }

            List<Message> messages = new ArrayList<>();
            for (AgentMessage msg : filtered) {
                ConversationRole role = "user".equals(msg.role()) ? ConversationRole.USER : ConversationRole.ASSISTANT;
                messages.add(Message.builder()
                        .role(role)
                        .content(ContentBlock.fromText(msg.content()))
                        .build());
            }

            if (messages.isEmpty()) return "Ciao! Come posso aiutarti?";

            String today = LocalDate.now().toString();
            String systemPrompt = String.format("""
                    Sei un assistente finanziario personale per la famiglia, integrato nell'app Gestione Spese.
                    Oggi è %s.

                    Il tuo ruolo è:
                    - Aiutare a registrare, consultare e gestire spese e budget
                    - Fornire consigli finanziari personalizzati basati sui dati reali dell'utente
                    - Rispondere in italiano in modo friendly e conciso
                    - Quando crei o modifichi dati, conferma sempre l'operazione eseguita

                    Per le categorie usa questi ID: alimentari, ristoranti, trasporti, casa, salute, intrattenimento, abbigliamento, istruzione, viaggi, altro
                    Quando l'utente non specifica la data, usa oggi. Usa i dati reali per i consigli personalizzati.
                    """, today);

            return converse(client, messages, systemPrompt, userEmail);

        } catch (Exception e) {
            return "Mi dispiace, si è verificato un errore: " + e.getMessage();
        }
    }

    private String converse(BedrockRuntimeClient client, List<Message> messages,
            String systemPrompt, String userEmail) throws Exception {

        ConverseRequest request = ConverseRequest.builder()
                .modelId(modelId)
                .messages(messages)
                .system(SystemContentBlock.fromText(systemPrompt))
                .toolConfig(buildToolConfig())
                .inferenceConfig(InferenceConfiguration.builder().maxTokens(1024).build())
                .build();

        ConverseResponse response = client.converse(request);
        StopReason stopReason = response.stopReason();

        if (stopReason == StopReason.END_TURN || stopReason == StopReason.MAX_TOKENS) {
            for (ContentBlock block : response.output().message().content()) {
                if (block.text() != null) return block.text();
            }
            return "Operazione completata.";
        }

        if (stopReason == StopReason.TOOL_USE) {
            messages.add(response.output().message());

            List<ContentBlock> toolResults = new ArrayList<>();
            for (ContentBlock block : response.output().message().content()) {
                if (block.toolUse() != null) {
                    ToolUseBlock toolUse = block.toolUse();
                    JsonNode inputNode = documentToJsonNode(toolUse.input());
                    String result = executeTool(toolUse.name(), inputNode, userEmail);
                    toolResults.add(ContentBlock.fromToolResult(
                            ToolResultBlock.builder()
                                    .toolUseId(toolUse.toolUseId())
                                    .content(ToolResultContentBlock.fromText(result))
                                    .build()));
                }
            }

            messages.add(Message.builder()
                    .role(ConversationRole.USER)
                    .content(toolResults)
                    .build());

            return converse(client, messages, systemPrompt, userEmail);
        }

        return "Operazione completata.";
    }

    // --- Tool execution ---

    private String executeTool(String toolName, JsonNode input, String userEmail) {
        try {
            return switch (toolName) {
                case "get_expenses" -> executeGetExpenses(input, userEmail);
                case "create_expense" -> executeCreateExpense(input, userEmail);
                case "delete_expense" -> executeDeleteExpense(input, userEmail);
                case "get_budgets" -> executeGetBudgets(input, userEmail);
                case "create_budget" -> executeCreateBudget(input, userEmail);
                case "get_spending_summary" -> executeGetSpendingSummary(input, userEmail);
                default -> "Tool non riconosciuto: " + toolName;
            };
        } catch (Exception e) {
            return "Errore: " + e.getMessage();
        }
    }

    private String executeGetExpenses(JsonNode input, String userEmail) throws Exception {
        int month = input.path("month").asInt(0);
        int year = input.path("year").asInt(0);
        String categoryId = input.path("category_id").asText(null);
        String search = input.path("search").asText(null);

        LocalDate dateFrom = null, dateTo = null;
        if (month > 0 && year > 0) {
            dateFrom = LocalDate.of(year, month, 1);
            dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());
        } else if (year > 0) {
            dateFrom = LocalDate.of(year, 1, 1);
            dateTo = LocalDate.of(year, 12, 31);
        }

        PagedExpenses result = expenseService.getExpenses(userEmail, 0, 50, "date,desc",
                dateFrom, dateTo, categoryId, null, null, search);
        return objectMapper.writeValueAsString(result);
    }

    private String executeCreateExpense(JsonNode input, String userEmail) throws Exception {
        double amount = input.path("amount").asDouble();
        String description = input.path("description").asText("");
        String categoryId = input.path("category_id").asText("altro");
        String dateStr = input.path("date").asText(null);
        String typeStr = input.path("type").asText("EXPENSE");

        LocalDate date = dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr) : LocalDate.now();
        TransactionType type = TransactionType.valueOf(typeStr.toUpperCase());

        ExpenseCreateRequest req = new ExpenseCreateRequest(amount, "EUR", categoryId, date, description, type, null, null);
        ExpenseDto result = expenseService.createExpense(userEmail, req);
        return objectMapper.writeValueAsString(result);
    }

    private String executeDeleteExpense(JsonNode input, String userEmail) {
        String expenseId = input.path("expense_id").asText();
        expenseService.deleteExpense(expenseId, userEmail);
        return "{\"success\": true, \"message\": \"Spesa eliminata con successo\"}";
    }

    private String executeGetBudgets(JsonNode input, String userEmail) throws Exception {
        var result = budgetService.getBudgets(userEmail, 0, 50, null, null, LocalDate.now());
        return objectMapper.writeValueAsString(result);
    }

    private String executeCreateBudget(JsonNode input, String userEmail) throws Exception {
        double amount = input.path("amount").asDouble();
        String categoryId = input.path("category_id").asText(null);
        BudgetPeriod period = BudgetPeriod.valueOf(input.path("period").asText("MONTHLY").toUpperCase());
        BudgetCreateRequest req = new BudgetCreateRequest(categoryId, amount, period, null, null, null);
        BudgetDto result = budgetService.createBudget(userEmail, req);
        return objectMapper.writeValueAsString(result);
    }

    private String executeGetSpendingSummary(JsonNode input, String userEmail) throws Exception {
        int month = input.path("month").asInt(LocalDate.now().getMonthValue());
        int year = input.path("year").asInt(LocalDate.now().getYear());

        LocalDate dateFrom = LocalDate.of(year, month, 1);
        LocalDate dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());

        PagedExpenses expenses = expenseService.getExpenses(userEmail, 0, 200, "date,desc",
                dateFrom, dateTo, null, null, null, null);

        java.util.Map<String, Double> byCategory = new java.util.LinkedHashMap<>();
        double total = 0;
        for (var e : expenses.content()) {
            if (e.type() == TransactionType.EXPENSE) {
                byCategory.merge(e.categoryId() != null ? e.categoryId() : "altro", e.amount(), Double::sum);
                total += e.amount();
            }
        }

        ObjectNode summary = objectMapper.createObjectNode();
        summary.put("period", month + "/" + year);
        summary.put("total_expenses", total);
        summary.set("by_category", objectMapper.valueToTree(byCategory));
        summary.put("expense_count", expenses.content().size());
        return objectMapper.writeValueAsString(summary);
    }

    // --- Converse API helpers ---

    private ToolConfiguration buildToolConfig() throws Exception {
        List<Tool> tools = List.of(
                buildTool("get_expenses", "Recupera le spese dell'utente, filtrabile per mese, anno, categoria o ricerca.",
                        """
                        {"type":"object","properties":{
                          "month":{"type":"integer","description":"Mese (1-12)"},
                          "year":{"type":"integer","description":"Anno es. 2025"},
                          "category_id":{"type":"string"},
                          "search":{"type":"string"}
                        }}"""),
                buildTool("create_expense", "Crea una nuova spesa o entrata.",
                        """
                        {"type":"object","required":["amount","description","category_id"],"properties":{
                          "amount":{"type":"number"},
                          "description":{"type":"string"},
                          "category_id":{"type":"string"},
                          "date":{"type":"string","description":"YYYY-MM-DD, default oggi"},
                          "type":{"type":"string","enum":["EXPENSE","INCOME"]}
                        }}"""),
                buildTool("delete_expense", "Elimina una spesa tramite ID.",
                        """
                        {"type":"object","required":["expense_id"],"properties":{
                          "expense_id":{"type":"string"}
                        }}"""),
                buildTool("get_budgets", "Recupera i budget con stato speso/rimanente.",
                        """
                        {"type":"object","properties":{}}"""),
                buildTool("create_budget", "Crea un budget per una categoria.",
                        """
                        {"type":"object","required":["amount","period"],"properties":{
                          "amount":{"type":"number"},
                          "category_id":{"type":"string"},
                          "period":{"type":"string","enum":["MONTHLY","WEEKLY","YEARLY"]}
                        }}"""),
                buildTool("get_spending_summary", "Riepilogo spese per categoria in un mese/anno.",
                        """
                        {"type":"object","properties":{
                          "month":{"type":"integer"},
                          "year":{"type":"integer"}
                        }}""")
        );
        return ToolConfiguration.builder().tools(tools).build();
    }

    private Tool buildTool(String name, String description, String schemaJson) throws Exception {
        JsonNode schemaNode = objectMapper.readTree(schemaJson);
        return Tool.builder()
                .toolSpec(ToolSpecification.builder()
                        .name(name)
                        .description(description)
                        .inputSchema(ToolInputSchema.builder()
                                .json(jsonNodeToDocument(schemaNode))
                                .build())
                        .build())
                .build();
    }

    // Convert JsonNode → AWS SDK Document
    private Document jsonNodeToDocument(JsonNode node) {
        if (node == null || node.isNull()) return Document.fromNull();
        if (node.isTextual()) return Document.fromString(node.asText());
        if (node.isNumber()) return Document.fromNumber(node.numberValue().toString());
        if (node.isBoolean()) return Document.fromBoolean(node.asBoolean());
        if (node.isArray()) {
            Document.ListBuilder builder = Document.listBuilder();
            node.forEach(child -> builder.addDocument(jsonNodeToDocument(child)));
            return builder.build();
        }
        if (node.isObject()) {
            Document.MapBuilder builder = Document.mapBuilder();
            node.fields().forEachRemaining(e -> builder.putDocument(e.getKey(), jsonNodeToDocument(e.getValue())));
            return builder.build();
        }
        return Document.fromNull();
    }

    // Convert AWS SDK Document → JsonNode
    private JsonNode documentToJsonNode(Document doc) {
        if (doc == null || doc.isNull()) return objectMapper.getNodeFactory().nullNode();
        if (doc.isString()) return objectMapper.getNodeFactory().textNode(doc.asString());
        if (doc.isNumber()) return objectMapper.getNodeFactory().numberNode(doc.asNumber().doubleValue());
        if (doc.isBoolean()) return objectMapper.getNodeFactory().booleanNode(doc.asBoolean());
        if (doc.isList()) {
            var arr = objectMapper.getNodeFactory().arrayNode();
            doc.asList().forEach(d -> arr.add(documentToJsonNode(d)));
            return arr;
        }
        if (doc.isMap()) {
            var obj = objectMapper.getNodeFactory().objectNode();
            doc.asMap().forEach((k, v) -> obj.set(k, documentToJsonNode(v)));
            return obj;
        }
        return objectMapper.getNodeFactory().nullNode();
    }
}
