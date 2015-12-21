package com.tibco.tcx.streambase.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.streambase.sb.CompiledTuple;
import com.streambase.sb.Schema;
import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Tuple;
import com.streambase.sb.Schema.Field;
import com.streambase.sb.operator.PreparedQuery;
import com.streambase.sb.operator.RowConsumer;
import com.streambase.sb.operator.TableAccessor;

@SuppressWarnings("unused")
public class TableAccessorUtils {
	class PreparedQueryCache {
		private ConcurrentHashMap<PreparedQueryKey, PreparedQuery> cache = new ConcurrentHashMap<PreparedQueryKey,PreparedQuery>();
		class PreparedQueryKey {
			TableAccessor tableAccessor;
			String predicate;
			Schema argsSchema;
			public PreparedQueryKey(TableAccessor tableAccessor,
					String predicate, Schema argsSchema) {
				super();
				this.tableAccessor = tableAccessor;
				this.predicate = predicate;
				this.argsSchema = argsSchema;
			}
			public TableAccessor getTableAccessor() {
				return tableAccessor;
			}
			public void setTableAccessor(TableAccessor tableAccessor) {
				this.tableAccessor = tableAccessor;
			}
			public String getPredicate() {
				return predicate;
			}
			public void setPredicate(String predicate) {
				this.predicate = predicate;
			}
			public Schema getArgsSchema() {
				return argsSchema;
			}
			public void setArgsSchema(Schema argsSchema) {
				this.argsSchema = argsSchema;
			}
			
		}
		public void put(TableAccessor tableAccessor, String predicate, Schema argsSchema, PreparedQuery pq) {
			cache.put(new PreparedQueryKey(tableAccessor, predicate, argsSchema), pq);
		}
		public void putIfAbsent(TableAccessor tableAccessor, String predicate, Schema argsSchema, PreparedQuery pq) {
			cache.putIfAbsent(new PreparedQueryKey(tableAccessor, predicate, argsSchema), pq);
		}
		public PreparedQuery get(TableAccessor tableAccessor, String predicate, Schema argsSchema) {
			return cache.get(new PreparedQueryKey(tableAccessor, predicate, argsSchema));
		}
		public void removeTableAccessor(TableAccessor tableAccessor) {
			Set<Map.Entry<PreparedQueryKey,PreparedQuery>> pqes = cache.entrySet();
			for (Map.Entry<PreparedQueryKey,PreparedQuery> e: pqes) {
				if (e.getKey().getTableAccessor() == tableAccessor) {
					pqes.remove(e);
				}
			}
		}
		public void clear() {
			cache.clear();
		}
	}

	private static PreparedQueryCache cache = new TableAccessorUtils().new PreparedQueryCache();
	
	public static PreparedQuery prepare(TableAccessor tableAccessor, String predicate, Schema argsSchema)
		throws StreamBaseException {
		PreparedQuery pq = cache.get(tableAccessor,predicate, argsSchema);
		if (pq == null) {
			pq = tableAccessor.prepare(predicate, argsSchema);
			cache.put(tableAccessor, predicate, argsSchema, pq);
		}
		return pq;
	}

	public static List<Tuple> getAllRows(TableAccessor tableAccessor)
			throws StreamBaseException {
		final List<Tuple> l = new ArrayList<Tuple>();
		PreparedQuery preparedQuery = prepare(tableAccessor, "true", Schema.EMPTY_SCHEMA);
		CompiledTuple preparedArgsTuple = preparedQuery.createArgumentTuple();
		preparedQuery.execute (preparedArgsTuple, new RowConsumer() {
			public void consume(Tuple row) throws StreamBaseException {
				try {
					l.add(row.clone());
				} catch (CloneNotSupportedException e) {
					throw new StreamBaseException(e);
				}
			}
		});
		return l;
	}
	
	public static void removeTableAccessor(TableAccessor tableAccessor) {
		cache.removeTableAccessor(tableAccessor);
	}

}	

