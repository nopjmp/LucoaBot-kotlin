package moe.giga.discord.permissions;

public enum AccessLevel {
    USER, MOD, ADMIN, ROOT;

    public static AccessLevel fromString(String str) {
        switch (str.toLowerCase()) {
            case "user":
                return USER;
            case "mod":
                return MOD;
            case "admin":
                return ADMIN;
            case "root":
                return ROOT;
        }
        return USER; // return user by default
    }

    @Override
    public String toString() {
        switch (this) {
            case USER:
                return "user";
            case MOD:
                return "mod";
            case ADMIN:
                return "admin";
            case ROOT:
                return "root";
        }
        return "(unknown)";
    }

}
