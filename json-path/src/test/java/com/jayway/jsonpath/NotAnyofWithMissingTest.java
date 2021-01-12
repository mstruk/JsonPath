package com.jayway.jsonpath;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.junit.Assert;
import org.junit.Test;

import static com.jayway.jsonpath.JsonPath.using;

public class NotAnyofWithMissingTest {

    @Test
    public void jaywayReproducer() throws JsonProcessingException {

        final String jsonString = "{ \n" +
                "  \"token\": {\n" +
                "    \"sub\": \"user1\", \n" +
                "    \"aud\": [\"uma_authorization\", \"kafka\"] \n" +
                "  }\n" +
                "}\n";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readValue(jsonString, JsonNode.class);

        Configuration conf = Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .options(Option.SUPPRESS_EXCEPTIONS)
                .build();

        ParseContext ctx = using(conf);
        DocumentContext doc = ctx.parse(json);



        //
        // Logical not (!) works correctly with anyof and noneof
        // when the targeted attribute is present
        //

        // returns item (filter result is true)
        String filter = "@.aud anyof ['kafka', 'something']";

        ArrayNode result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 1", 1, result.size());

        // returns nothing (filter result is false)
        filter = "!(@.aud anyof ['kafka', 'something'])";

        result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 0", 0, result.size());

        // returns item (filter result is true)
        filter = "@.aud noneof ['app1', 'app2']";

        result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 1", 1, result.size());

        // returns nothing (filter result is false)
        filter = "!(@.aud noneof ['app1', 'app2'])";

        result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 0", 0, result.size());




        //
        // Logical not (!) does NOT work correctly with anyof and noneof
        // when the targeted attribute is missing
        //

        // returns nothing (filter result is false)
        filter = "@.missing anyof ['kafka', 'something']";

        result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 0", 0, result.size());

        // returns nothing (filter result is false)
        // BUG?: !false should always evaluate to true
        filter = "!(@.missing anyof ['kafka', 'something'])";

        result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 1", 1, result.size());      // <<<<  FAILS

        // returns nothing (filter result is false)
        filter = "@.missing noneof ['kafka', 'something']";

        result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 0", 0, result.size());

        // returns nothing (filter result is false)
        // BUG?: !false should always evaluate to true
        filter = "!(@.missing noneof ['kafka', 'something'])";

        result = doc.read("$[*][?(" + filter + ")]");
        Assert.assertEquals("Result size should be 1", 1, result.size());      // <<<<  FAILS
    }
}
