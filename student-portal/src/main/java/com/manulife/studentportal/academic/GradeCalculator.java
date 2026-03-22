package com.manulife.studentportal.academic;

public final class GradeCalculator {

    private GradeCalculator() {
    }

    public static String calculateGrade(double score, int fullMarks) {
        if (fullMarks <= 0) {
            throw new IllegalArgumentException("Full marks must be greater than 0");
        }
        double percentage = (score / fullMarks) * 100.0;
        return gradeFromPercentage(percentage);
    }

    public static String gradeFromPercentage(double percentage) {
        if (percentage >= 90.0) return "A";
        if (percentage >= 80.0) return "B";
        if (percentage >= 70.0) return "C";
        if (percentage >= 60.0) return "D";
        return "F";
    }

    public static double calculatePercentage(double score, int fullMarks) {
        if (fullMarks <= 0) return 0.0;
        return Math.round((score / fullMarks) * 100.0 * 100.0) / 100.0; // 2 decimal places
    }
}