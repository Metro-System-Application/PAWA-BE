package pawa_be.profile.internal.enumeration;

public enum ImageType {
    STUDENT_ID_FRONT,
    STUDENT_ID_BACK,
    NATIONAL_ID_FRONT,
    NATIONAL_ID_BACK,
    USER_PROFILE;

    public static boolean isMatchingPair(ImageType front, ImageType back) {
        return (front == STUDENT_ID_FRONT && back == STUDENT_ID_BACK) ||
                (front == NATIONAL_ID_FRONT && back == NATIONAL_ID_BACK);
    }
}