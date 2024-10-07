package fr.axopen.sonar.severity.customization.metric;

import org.sonar.api.issue.impact.Severity;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.List;
import java.util.Locale;

public class SeverityCustomizationMetrics implements Metrics {

    private static String severityName(Severity severity) {
        switch (severity) {
            case LOW:
                return "Impacte mineure";
            case MEDIUM:
                return "Impacte majeure";
            case HIGH:
                return "Impacte critique";
            default:
                throw new IllegalArgumentException("Unknown severity: " + severity);
        }
    }


    private static Metric<Integer> buildCustomSeverityMetric(Severity severity) {
        return new Metric.Builder(
                "custom_severity_" + severity.name() + "_metric",
                severityName(severity),
                Metric.ValueType.INT
        )
                .setDescription("Metrique basée sur le niveau d'" + severityName(severity).toLowerCase(Locale.ROOT))
                .setDirection(Metric.DIRECTION_WORST)
                .setQualitative(true)
                .setDomain("Impacte")
                .create();
    }

    private static Metric<Integer> buildCustomNewSeverityMetric(Severity severity) {
        return new Metric.Builder(
                "new_custom_severity_" + severity.name() + "_metric",
                severityName(severity),
                Metric.ValueType.INT
        )
                .setDescription("Metrique basée sur le niveau d'" + severityName(severity).toLowerCase(Locale.ROOT) + " du nouveau code")
                .setDirection(Metric.DIRECTION_WORST)
                .setQualitative(true)
                .setDomain("Impacte")
                .setDeleteHistoricalData(true)
                .create();
    }


    public static final Metric<Integer> SEVERITY_HIGH_METRIC = buildCustomSeverityMetric(Severity.HIGH);
    public static final Metric<Integer> SEVERITY_MEDIUM_METRIC = buildCustomSeverityMetric(Severity.MEDIUM);
    public static final Metric<Integer> SEVERITY_LOW_METRIC = buildCustomSeverityMetric(Severity.LOW);

    public static final Metric<Integer> NEW_SEVERITY_HIGH_METRIC = buildCustomNewSeverityMetric(Severity.HIGH);
    public static final Metric<Integer> NEW_SEVERITY_MEDIUM_METRIC = buildCustomNewSeverityMetric(Severity.MEDIUM);

    @Override
    public List<Metric> getMetrics() {
        return List.of(SEVERITY_HIGH_METRIC, SEVERITY_MEDIUM_METRIC, SEVERITY_LOW_METRIC,
                NEW_SEVERITY_HIGH_METRIC, NEW_SEVERITY_MEDIUM_METRIC);
    }
}
