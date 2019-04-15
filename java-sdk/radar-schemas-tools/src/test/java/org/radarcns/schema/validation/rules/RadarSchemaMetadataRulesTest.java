/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.schema.validation.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.radarcns.schema.validation.ValidationHelper.COMMONS_PATH;
import static org.radarcns.schema.Scope.MONITOR;
import static org.radarcns.schema.Scope.PASSIVE;
import static org.radarcns.schema.specification.SourceCatalogue.BASE_PATH;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.schema.validation.SchemaValidator;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.ValidationHelper;
import org.radarcns.schema.validation.config.ExcludeConfig;

/**
 * TODO.
 */
public class RadarSchemaMetadataRulesTest {

    private static final String RECORD_NAME_MOCK = "RecordName";
    private RadarSchemaMetadataRules validator;

    @Before
    public void setUp() {
        ExcludeConfig config = new ExcludeConfig();
        validator = new RadarSchemaMetadataRules(BASE_PATH, config);
    }

    @Test
    public void fileNameTest() {
        assertEquals("Questionnaire",
                ValidationHelper.getRecordName(Paths.get("/path/to/questionnaire.avsc")));
        assertEquals("ApplicationExternalTime",
                ValidationHelper.getRecordName(
                        Paths.get("/path/to/application_external_time.avsc")));
    }

    @Test
    public void nameSpaceInvalidPlural() {
        Schema schema = SchemaBuilder
                .builder("org.radarcns.monitors.test")
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        Path root = MONITOR.getPath(BASE_PATH.resolve(COMMONS_PATH));
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Stream<ValidationException> result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, MONITOR, path));

        assertEquals(1, result.count());
    }

    @Test
    public void nameSpaceInvalidLastPartPlural() {

        Schema schema = SchemaBuilder
                    .builder("org.radarcns.monitor.tests")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .endRecord();

        Path root = MONITOR.getPath(BASE_PATH.resolve(COMMONS_PATH));
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Stream<ValidationException> result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, MONITOR, path));

        assertEquals(1, result.count());
    }

    @Test
    public void recordNameTest() {
        // misspell aceleration
        String fieldName = "EmpaticaE4Aceleration";
        Path filePath = Paths.get("/path/to/empatica_e4_acceleration.avsc");

        Schema schema = SchemaBuilder
                .builder("org.radarcns.passive.empatica")
                .record(fieldName)
                .fields()
                .endRecord();

        Stream<ValidationException> result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, PASSIVE, filePath));

        assertEquals(2, result.count());

        fieldName = "EmpaticaE4Acceleration";
        filePath = BASE_PATH.resolve("commons/passive/empatica/empatica_e4_acceleration.avsc");

        schema = SchemaBuilder
                .builder("org.radarcns.passive.empatica")
                .record(fieldName)
                .fields()
                .endRecord();

        result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, PASSIVE, filePath));

        assertEquals("", SchemaValidator.format(result));
    }
}
