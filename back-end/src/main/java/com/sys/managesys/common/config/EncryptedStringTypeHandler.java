package com.sys.managesys.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 지정한 컬럼(개인정보)을 저장 시 자동 암호화 / 조회 시 자동 복호화하는 MyBatis TypeHandler.
 * 전역 자동 적용을 피하기 위해 @MappedTypes 를 두지 않고, 매퍼에서 컬럼 단위로 명시 지정한다.
 *   - INSERT/UPDATE: #{prop, typeHandler=com.sys.managesys.common.config.EncryptedStringTypeHandler}
 *   - SELECT(resultMap): <result column="..." property="..." typeHandler="...EncryptedStringTypeHandler"/>
 */
public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, EncryptionUtil.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return EncryptionUtil.decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return EncryptionUtil.decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return EncryptionUtil.decrypt(cs.getString(columnIndex));
    }
}
