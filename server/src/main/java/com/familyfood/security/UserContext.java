package com.familyfood.security;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> FAMILY_ID = new ThreadLocal<>();

    public static void set(Long userId, Long familyId) { USER_ID.set(userId); FAMILY_ID.set(familyId); }
    public static Long getUserId() { return USER_ID.get(); }
    public static Long getFamilyId() { return FAMILY_ID.get(); }
    public static void clear() { USER_ID.remove(); FAMILY_ID.remove(); }
}
