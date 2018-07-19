package uk.gov.gchq.palisade.resource.service;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.regex.Pattern;

public class HDFSResourceDetails {
    public static final String TYPE_DEL = "_";
    public static final String FORMAT_DEL = ".";
    public static final String FILE_NAME_FORMAT = "%s" + TYPE_DEL + "%s" + FORMAT_DEL + "%s";
    private String connectionDetail, type, format;

    public HDFSResourceDetails(Resource resource) {
//        this(resource.getId(), resource.getType(), resource.getFormat());
    }

    public HDFSResourceDetails(final String connectionDetail, final String type, final String format, final String name) {
        this.connectionDetail = connectionDetail;
        this.type = type;
        this.format = format;
    }

    public String getConnectionDetail() {
        return connectionDetail;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    protected static HDFSResourceDetails getResourceDetailsFromPath(final String path) {
        //The mirror of the FILE_NAME_FORMAT
        final String[] split = path.split(Pattern.quote("/"));
        final String fileString = split[split.length - 1];
        final String[] typeSplit = fileString.split(TYPE_DEL);
        if (typeSplit.length == 2) {
            final String type = typeSplit[0];
            final String[] idSplit = typeSplit[1].split(Pattern.quote(FORMAT_DEL));
            if (idSplit.length == 2) {
                final String name = idSplit[0];
                final String format = idSplit[1];

                return new HDFSResourceDetails(path, type, format, name);
            }
        }
        throw new IllegalArgumentException("Incorrect format expected:" + FILE_NAME_FORMAT + " found: " + fileString);
    }

    protected static String getFileNameFromResourceDetails(final String name, final String type, final String format) {
        //Type, Id, Format
        return String.format(FILE_NAME_FORMAT, type, name, format);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("id", connectionDetail)
                .append("format", format)
                .build();
    }
}
