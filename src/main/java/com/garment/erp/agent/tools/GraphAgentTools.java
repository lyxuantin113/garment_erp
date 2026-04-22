package com.garment.erp.agent.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GraphAgentTools {

    private final Driver neo4jDriver;

    // Record định nghĩa Input cho Function
    public record GraphQueryRequest(String cypherQuery) {
    }

    // Record định nghĩa Output của Function
    public record GraphQueryResponse(String result) {
    }

    @Bean
    @Description("Executes a read-only Cypher query to retrieve specific nodes, edges, or paths from the Neo4j Knowledge Graph. ALWAYS use this to understand the policy before answering.")
    public Function<GraphQueryRequest, GraphQueryResponse> queryGraph() {
        return request -> {
            log.info("🤖 [Agent Tool] Thực thi Cypher: \n{}", request.cypherQuery());
            try (Session session = neo4jDriver.session()) {
                Result result = session.run(request.cypherQuery());
                // Chuyển kết quả về dạng List<Map> để Agent dễ đọc hiểu JSON/String fallback
                List<Map<String, Object>> records = result.list(r -> r.asMap());
                String stringResult = records.isEmpty() ? "No results found." : records.toString();
                log.info("🤖 [Agent Tool] Trả kết quả: {} records.", records.size());
                return new GraphQueryResponse(stringResult);
            } catch (Exception e) {
                log.error("🤖 [Agent Tool] Lỗi truy vấn Graph: ", e);
                return new GraphQueryResponse("Error executing query: " + e.getMessage());
            }
        };
    }
}
