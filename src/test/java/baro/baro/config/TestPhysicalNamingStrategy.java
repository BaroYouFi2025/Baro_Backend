package baro.baro.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

// 테스트에서만 사용하는 단순 snake_case 변환 네이밍 전략.
// 운영 설정과 동일한 룰을 보장해 스키마 생성 시 컬럼명이 일관되도록 한다.
public class TestPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return apply(name, context);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return apply(name, context);
    }

    private Identifier apply(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }
        String snakeCase = toSnakeCase(name.getText());
        return Identifier.toIdentifier(snakeCase, name.isQuoted());
    }

    private String toSnakeCase(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }
}
