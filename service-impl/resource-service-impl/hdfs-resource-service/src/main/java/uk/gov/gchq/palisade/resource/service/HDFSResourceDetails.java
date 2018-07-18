package uk.gov.gchq.palisade.resource.service;

import java.util.regex.Pattern;

public class HDFSResourceDetails {
    public static final String TYPE_DEL = "_";
    public static final String FORMAT_DEL = ".";
    public static final String FILE_NAME_FORMAT = "%s" + TYPE_DEL + "%s" + FORMAT_DEL + "%s";
    private String id, type, format;

    public HDFSResourceDetails(final String id, final String type, final String format) {
        this.id = id;
        this.type = type;
        this.format = format;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    protected static HDFSResourceDetails getResourceDetailsFromFileName(final String fileName) {
        //The refection of FILE_NAME_FORMAT

        final String[] typeSplit = fileName.split(TYPE_DEL);
        if (typeSplit.length == 2) {
            final String type = typeSplit[0];
            final String[] idSplit = typeSplit[1].split(Pattern.quote(FORMAT_DEL));
            if (idSplit.length == 2) {
                final String id = idSplit[0];
                final String format = idSplit[1];

                return new HDFSResourceDetails(id, type, format);
            }
        }
        throw new IllegalArgumentException("Incorrect format expected:" + FILE_NAME_FORMAT + " found: " + fileName);
    }

    protected static String getFileNameFromResourceDetails(final HDFSResourceDetails resourceDetails) {
        return getFileNameFromResourceDetails(resourceDetails.getId(), resourceDetails.getType(), resourceDetails.getFormat());
    }

    protected static String getFileNameFromResourceDetails(final String id, final String type, final String format) {
        //Type, Id, Format
        return String.format(FILE_NAME_FORMAT, type, id, format);
    }
}
