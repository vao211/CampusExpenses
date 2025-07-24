package com.example.vcampusexpenses.utils;

import java.util.UUID;

public class IdGenerator {
    public enum ModelType {
        BUDGET("BUDGET"),
        ACCOUNT("ACCOUNT"),
        CATEGORY("CATEGORY"),
        TRANSACTION("TRANSACTION");

        private final String prefix;

        ModelType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    /**
     * @param modelType Loại model (BUDGET, ACCOUNT, CATEGORY, TRANSACTION)
     * @return ID dạng <PREFIX>-<UUID>
     */
    public static String generateId(ModelType modelType) {
        return modelType.getPrefix() + "-" + UUID.randomUUID().toString();
    }

    /**
     * @param customPrefix Tiền tố tùy chỉnh
     * @return ID dạng <customPrefix>-<UUID>
     */
    public static String generateId(String customPrefix) {
        return customPrefix + "-" + UUID.randomUUID().toString();
    }
}
