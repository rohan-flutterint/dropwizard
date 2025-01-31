package io.dropwizard.migrations;

import liquibase.change.CheckSum;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class DbCalculateChecksumCommandTest {

    private final DbCalculateChecksumCommand<TestMigrationConfiguration> migrateCommand = new DbCalculateChecksumCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    void testRun() throws Exception {
        final AtomicBoolean checkSumVerified = new AtomicBoolean();
        migrateCommand.setCheckSumConsumer(checkSum -> {
            assertThat(checkSum).isEqualTo(CheckSum.parse("9:6e43b880df9a2b41d436026d8a09c457"));
            checkSumVerified.set(true);
        });
        migrateCommand.run(null, new Namespace(Map.of(
                "id", List.of("2"),
                "author", List.of("db_dev"))),
            MigrationTestSupport.createConfiguration());
        assertThat(checkSumVerified).isTrue();
    }

    @Test
    void testHelpPage() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        MigrationTestSupport.createSubparser(migrateCommand).printHelp(new PrintWriter(new OutputStreamWriter(out, UTF_8), true));
        assertThat(out.toString(UTF_8)).isEqualToNormalizingNewlines(
            "usage: db calculate-checksum [-h] [--migrations MIGRATIONS-FILE]\n" +
                "          [--catalog CATALOG] [--schema SCHEMA]\n" +
                "          [--analytics-enabled ANALYTICS-ENABLED] [file] id author\n" +
                "\n" +
                "Calculates and prints a checksum for a change set\n" +
                "\n" +
                "positional arguments:\n" +
                "  file                   application configuration file\n" +
                "  id                     change set id\n" +
                "  author                 author name\n" +
                "\n" +
                "named arguments:\n" +
                "  -h, --help             show this help message and exit\n" +
                "  --migrations MIGRATIONS-FILE\n" +
                "                         the file containing  the  Liquibase migrations for\n" +
                "                         the application\n" +
                "  --catalog CATALOG      Specify  the   database   catalog   (use  database\n" +
                "                         default if omitted)\n" +
                "  --schema SCHEMA        Specify the database schema  (use database default\n" +
                "                         if omitted)\n" +
                "  --analytics-enabled ANALYTICS-ENABLED\n" +
                "                         This turns on analytics  gathering for that single\n" +
                "                         occurrence of a command.\n");
    }
}
