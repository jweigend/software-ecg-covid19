package de.qaware.ekg.awb.da.elasticsearch.utils;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;

import static de.qaware.ekg.awb.da.elasticsearch.utils.SchemaBuilder.DOC_TYPE_KEY;

public class IndexBuilder {

    private final RestHighLevelClient client;

    public IndexBuilder() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
    }

    public static void main(String[] argc) {
        IndexBuilder indexBuilder = new IndexBuilder();
        indexBuilder.deleteIndex();
        indexBuilder.createIndex();

        try {
            indexBuilder.client.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }


    public void createIndex() {

        try {
            CreateIndexRequest request = new CreateIndexRequest("software-ekg-data");
            setupIndexProperties(request);
            defineSchema(request);

            client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteIndex() {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest("software-ekg-data");
            client.indices().delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    private static void setupIndexProperties(CreateIndexRequest request) {
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1)
        );
    }

    private static void defineSchema(CreateIndexRequest request) throws IOException {
        request.mapping(DOC_TYPE_KEY, SchemaBuilder.buildSchema());

    }
}
