package org.oddlama.vane.core.config;

import static org.oddlama.vane.util.Util.namespaced_key;

import java.lang.StringBuilder;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.configuration.file.YamlConfiguration;

import org.oddlama.vane.annotation.config.ConfigStringList;
import org.oddlama.vane.core.YamlLoadException;

public class ConfigStringListField extends ConfigField<List<String>> {
	public ConfigStringList annotation;

	public ConfigStringListField(Object owner, Field field, Function<String, String> map_name, ConfigStringList annotation) {
		super(owner, field, map_name, "list of strings", annotation.desc());
		this.annotation = annotation;
	}

	private void append_string_list_defintion(StringBuilder builder, String indent, String prefix) {
		append_list_definition(builder, indent, prefix, def(), (b, s) -> {
				b.append("\"");
				b.append(s.replace("\"", "\\\"")); // FIXME use proper yaml escaping.. Why does't bukkit have a method for that...
				b.append("\"");
			});
	}

	@Override
	public List<String> def() {
		final var override = overridden_def();
		if (override != null) {
			return override;
		} else {
			return Arrays.asList(annotation.def());
		}
	}

	@Override
	public void generate_yaml(StringBuilder builder, String indent) {
		append_description(builder, indent);

		// Default
		builder.append(indent);
		builder.append("# Default:\n");
		append_string_list_defintion(builder, indent, "# ");

		// Definition
		builder.append(indent);
		builder.append(basename());
		builder.append(":\n");
		append_string_list_defintion(builder, indent, "");
	}

	@Override
	public void check_loadable(YamlConfiguration yaml) throws YamlLoadException {
		check_yaml_path(yaml);

		if (!yaml.isList(yaml_path())) {
			throw new YamlLoadException("Invalid type for yaml path '" + yaml_path() + "', expected list");
		}

		for (var obj : yaml.getList(yaml_path())) {
			if (!(obj instanceof String)) {
				throw new YamlLoadException("Invalid type for yaml path '" + yaml_path() + "', expected string");
			}
		}
	}

	public void load(YamlConfiguration yaml) {
		final var list = new ArrayList<>();
		for (var obj : yaml.getList(yaml_path())) {
			list.add((String)obj);
		}

		try {
			field.set(owner, list);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Invalid field access on '" + field.getName() + "'. This is a bug.");
		}
	}
}
