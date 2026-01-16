package net.yudichev.jiotty.user.ui;

import java.util.List;
import java.util.Map;

/// Plain data records for Options to be serialized to JSON and rendered on the client.
public final class OptionDtos {
    private OptionDtos() {
    }

    public sealed interface OptionDto permits Text, TextArea, Checkbox, Time, Duration, Select, MultiSelect, Chat {}

    public record Text(String type, String key, String label, String tabName, int order, String value) implements OptionDto {}

    public record TextArea(String type, String key, String label, String tabName, int order, String value, int rows) implements OptionDto {}

    public record Checkbox(String type, String key, String label, String tabName, int order, boolean checked) implements OptionDto {}

    public record Time(String type, String key, String label, String tabName, int order, String value) implements OptionDto {}

    public record Duration(String type, String key, String label, String tabName, int order, String valueHuman, String placeholder, String help)
            implements OptionDto {}

    public record Select(String type, String key, String label, String tabName, int order, List<String> options, String value) implements OptionDto {}

    public record MultiSelect(String type, String key, String label, String tabName, int order, Map<String, String> allOptions, List<String> selectedIds)
            implements OptionDto {}

    public record Chat(String type, String key, String label, String tabName, int order, String historyText) implements OptionDto {}
}
