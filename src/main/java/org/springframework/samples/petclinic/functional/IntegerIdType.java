/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.functional;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.EnhancedUserType;
import org.jspecify.annotations.Nullable;

/**
 * Hibernate {@link EnhancedUserType} base that maps a single-{@code int} typed id (a
 * {@code value record} such as {@code OwnerId}) to an {@code INTEGER} column. Unlike a
 * JPA {@code AttributeConverter} — which Hibernate forbids on {@code @Id} — a
 * {@code UserType} is permitted on an identifier and cooperates with
 * {@code GenerationType.IDENTITY}.
 *
 * <p>
 * Concrete ids need only a tiny no-arg subclass, e.g. {@code class OwnerIdType extends
 * IntegerIdType<OwnerId> { OwnerIdType() { super(OwnerId.class, OwnerId::new,
 * OwnerId::id); } }}.
 *
 * @param <ID> the typed id
 */
public abstract class IntegerIdType<ID> implements EnhancedUserType<ID> {

	private final Class<ID> idClass;

	private final IntFunction<ID> fromInt;

	private final ToIntFunction<ID> toInt;

	protected IntegerIdType(Class<ID> idClass, IntFunction<ID> fromInt, ToIntFunction<ID> toInt) {
		this.idClass = idClass;
		this.fromInt = fromInt;
		this.toInt = toInt;
	}

	@Override
	public int getSqlType() {
		return Types.INTEGER;
	}

	@Override
	public Class<ID> returnedClass() {
		return this.idClass;
	}

	@Override
	public boolean equals(@Nullable ID x, @Nullable ID y) {
		return Objects.equals(x, y);
	}

	@Override
	public int hashCode(ID x) {
		return x.hashCode();
	}

	@Override
	public @Nullable ID nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session,
			@Nullable Object owner) throws SQLException {
		int value = rs.getInt(position);
		return rs.wasNull() ? null : this.fromInt.apply(value);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, @Nullable ID value, int index,
			SharedSessionContractImplementor session) throws SQLException {
		if (value == null) {
			st.setNull(index, Types.INTEGER);
		}
		else {
			st.setInt(index, this.toInt.applyAsInt(value));
		}
	}

	@Override
	public @Nullable ID deepCopy(@Nullable ID value) {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public @Nullable Serializable disassemble(@Nullable ID value) {
		return (value == null) ? null : this.toInt.applyAsInt(value);
	}

	@Override
	public @Nullable ID assemble(@Nullable Serializable cached, @Nullable Object owner) {
		return (cached == null) ? null : this.fromInt.apply((Integer) cached);
	}

	@Override
	public String toSqlLiteral(ID value) {
		return Integer.toString(this.toInt.applyAsInt(value));
	}

	@Override
	public String toString(ID value) {
		return Integer.toString(this.toInt.applyAsInt(value));
	}

	@Override
	public ID fromStringValue(CharSequence sequence) {
		return this.fromInt.apply(Integer.parseInt(sequence.toString()));
	}

}
