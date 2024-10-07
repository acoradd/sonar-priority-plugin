/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package fr.axopen.sonar.severity.customization.measure;

import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.issue.IssueStatus;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.measures.Metric;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.axopen.sonar.severity.customization.metric.SeverityCustomizationMetrics.*;

public class SeverityMeasureComputer implements MeasureComputer {

    @Override
    public MeasureComputerDefinition define(MeasureComputerDefinitionContext def) {
        return def.newDefinitionBuilder()
                .setInputMetrics()
                .setOutputMetrics(SEVERITY_HIGH_METRIC.key(), SEVERITY_MEDIUM_METRIC.key(), SEVERITY_LOW_METRIC.key(),
                        NEW_SEVERITY_HIGH_METRIC.key(), NEW_SEVERITY_MEDIUM_METRIC.key())
                .build();
    }

    @Override
    public void compute(MeasureComputerContext context) {

        Map<Severity, Integer> groupByImpact = context.getIssues().stream()
                .filter(issue -> issue.issueStatus() == IssueStatus.OPEN)
                .map(issue -> issue.impacts().values().stream().max(this::compareSeverity).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(severity -> severity, Collectors.summingInt(e -> 1)));

        int highSeverity = groupByImpact.getOrDefault(Severity.HIGH, 0) + getChildImpactCount(context, SEVERITY_HIGH_METRIC);
        int mediumSeverity = groupByImpact.getOrDefault(Severity.MEDIUM, 0) + getChildImpactCount(context, SEVERITY_MEDIUM_METRIC);
        int lowSeverity = groupByImpact.getOrDefault(Severity.LOW, 0) + getChildImpactCount(context, SEVERITY_LOW_METRIC);

        if (highSeverity > 0) {
            context.addMeasure(SEVERITY_HIGH_METRIC.key(), highSeverity);
        }
        if (mediumSeverity > 0) {
            context.addMeasure(SEVERITY_MEDIUM_METRIC.key(), mediumSeverity);
        }
        if (lowSeverity > 0) {
            context.addMeasure(SEVERITY_LOW_METRIC.key(), lowSeverity);
        }


        if (highSeverity > 0) {
            context.addMeasure(NEW_SEVERITY_HIGH_METRIC.key(), highSeverity);
        }
        if (mediumSeverity > 0) {
            context.addMeasure(NEW_SEVERITY_MEDIUM_METRIC.key(), mediumSeverity);
        }
    }

    private int getChildImpactCount(MeasureComputerContext context, Metric<Integer> severityHighMetric) {
        Iterator<Measure> childrenMeasures = context.getChildrenMeasures(severityHighMetric.key()).iterator();
        Measure measure;
        int count = 0;
        while (childrenMeasures.hasNext()) {
            measure = childrenMeasures.next();
            if (measure != null) {
                count += measure.getIntValue();
            }
        }
        return count;
    }


    private int compareSeverity(Severity a, Severity b) {
        return severityToPriority(a) - severityToPriority(b);
    }

    private int severityToPriority(Severity severity) {
        switch (severity) {
            case LOW:
                return 1;
            case MEDIUM:
                return 2;
            case HIGH:
                return 3;
        }
        return -1;
    }
}
