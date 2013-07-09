package com.emc.greenplum.gpdb.hdfsconnector;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import com.emc.greenplum.gpdb.hadoop.io.GPDBWritable;
import com.emc.greenplum.gpdb.hdfsconnector.ColumnDescriptor;
import com.emc.greenplum.gpdb.hdfsconnector.HDFSMetaData;
import com.emc.greenplum.gpdb.hdfsconnector.IFieldsResolver;
import com.emc.greenplum.gpdb.hdfsconnector.OneField;
import com.emc.greenplum.gpdb.hdfsconnector.OneRow;
import com.gopivotal.mapred.input.JsonInputFormat;

public class JsonResolver implements IFieldsResolver {

	private ArrayList<OneField> list = new ArrayList<OneField>();
	private HDFSMetaData meta = null;

	public JsonResolver(HDFSMetaData meta) throws IOException {
		this.meta = meta;
	}

	@Override
	public List<OneField> GetFields(OneRow row) throws Exception {
		list.clear();

		// key is a Text object
		JsonNode root = JsonInputFormat.decodeLineToJsonNode(row.getData()
				.toString());

		if (root != null) {
			for (int i = 0; i < meta.columns(); ++i) {
				ColumnDescriptor cd = meta.getColumn(i);

				String[] tokens = cd.columnName().split("\\.");

				JsonNode node = root;
				for (int j = 0; j < tokens.length - 1; ++j) {
					node = node.path(tokens[j]);
				}

				// if the last token is an array index
				if (tokens[tokens.length - 1].contains("[")
						&& tokens[tokens.length - 1].contains("]")) {
					String token = tokens[tokens.length - 1].substring(0,
							tokens[tokens.length - 1].indexOf('['));

					int arrayIndex = Integer.parseInt(tokens[tokens.length - 1]
							.substring(
									tokens[tokens.length - 1].indexOf('[') + 1,
									tokens[tokens.length - 1].length() - 1));

					node = node.get(token);

					if (node == null || node.isMissingNode()) {
						list.add(null);
					} else if (node.isArray()) {
						int count = 0;
						boolean added = false;
						for (Iterator<JsonNode> arrayNodes = node.getElements(); arrayNodes
								.hasNext();) {
							JsonNode arrayNode = arrayNodes.next();

							if (count == arrayIndex) {
								added = true;
								addOneFieldToRecord(list, cd.columnType(),
										arrayNode);
								break;
							}

							++count;
						}

						// if we reached the end of the array without adding a
						// field, add null
						if (!added) {
							list.add(null);
						}

					} else {
						throw new InvalidParameterException(token
								+ " is not an array node");
					}

				} else {
					node = node.get(tokens[tokens.length - 1]);
					if (node == null || node.isMissingNode()) {
						list.add(null);
					} else {
						addOneFieldToRecord(list, cd.columnType(), node);
					}
				}
			}
		}

		return list;
	}

	private void addOneFieldToRecord(List<OneField> record,
			int gpdbWritableType, JsonNode val) throws IOException {
		OneField oneField = new OneField();
		oneField.type = gpdbWritableType;
		switch (gpdbWritableType) {
		case GPDBWritable.BIGINT:
			oneField.val = val.asLong();
			break;
		case GPDBWritable.BOOLEAN:
			oneField.val = val.asBoolean();
			break;
		case GPDBWritable.BPCHAR:
		case GPDBWritable.CHAR:
			oneField.val = val.asText().charAt(0);
			break;
		case GPDBWritable.BYTEA:
			oneField.val = val.asText().getBytes();
			break;
		case GPDBWritable.FLOAT8:
		case GPDBWritable.REAL:
			oneField.val = val.asDouble();
			break;
		case GPDBWritable.INTEGER:
		case GPDBWritable.SMALLINT:
			oneField.val = val.asInt();
			break;
		case GPDBWritable.TEXT:
		case GPDBWritable.VARCHAR:
			oneField.val = val.asText();
			break;
		default:
			throw new IOException("Unsupported type " + gpdbWritableType);
		}

		record.add(oneField);
	}
}