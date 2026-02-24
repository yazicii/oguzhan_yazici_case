package org.insider.data;

/**
 * Holds job card information (h2/title, department, location) for verification on the Lever detail page.
 */
public record JobCardInfo(
        String title,
        String location,
        String department
) {
    /** Normalizes text for comparison (trim, remove trailing slashes). */
    public static String normalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.trim().replaceAll("/+\\s*$", "").trim();
    }

    public boolean titleMatches(String leverTitle) {
        return containsNormalized(leverTitle, title);
    }

    public boolean locationMatches(String leverLocation) {
        return containsNormalized(leverLocation, location);
    }

    public boolean departmentMatches(String leverDepartment) {
        return containsNormalized(leverDepartment, department);
    }

    private static boolean containsNormalized(String haystack, String needle) {
        if (needle == null || needle.isBlank()) return true;
        String h = normalize(haystack).toLowerCase();
        String n = normalize(needle).toLowerCase();
        return h.contains(n) || n.contains(h);
    }
}
