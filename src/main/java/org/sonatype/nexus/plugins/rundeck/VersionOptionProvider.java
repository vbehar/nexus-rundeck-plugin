package org.sonatype.nexus.plugins.rundeck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.SearchType;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.metadata.operations.ComparableVersion;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Option provider for RunDeck - see http://rundeck.org/docs/RunDeck-Guide.html#option-model-provider<br>
 * Provider for version of artifacts presents in the Nexus repository, and matching the request.
 * 
 * @author Vincent Behar
 */
@Component(role = PlexusResource.class, hint = "VersionOptionProvider")
public class VersionOptionProvider extends AbstractOptionProvider {

    @Inject
    @Named("mavenCoordinates")
    private Searcher searcher;

    @Override
    public String getResourceUri() {
        return "/rundeck/options/version";
    }

    @Override
    public Object get(Context context, Request request, Response response, Variant variant) throws ResourceException {
        // retrieve main parameters (r, g, a, v, p, c)
        Form form = request.getResourceRef().getQueryAsForm();
        String repositoryId = form.getFirstValue("r", null);
        Map<String, String> terms = new HashMap<String, String>(form.getValuesMap());

        // search
        IteratorSearchResponse searchResponse;
        try {
            searchResponse = searcher.flatIteratorSearch(terms,
                                                         repositoryId,
                                                         null,
                                                         null,
                                                         null,
                                                         false,
                                                         SearchType.EXACT,
                                                         null);
        } catch (NoSuchRepositoryException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No repository at " + repositoryId, e);
        }

        // retrieve unique versions and sort them from newest to oldest
        List<Option> versions = new ArrayList<Option>();
        for (ArtifactInfo aInfo : searchResponse.getResults()) {
            if (!optionsContainsValue(versions, aInfo.version)) {
                versions.add(new Option(buildOptionName(aInfo), aInfo.version));
            }
        }
        Collections.sort(versions, new Comparator<Option>() {

            public int compare(Option o1, Option o2) {
                if (o1 == null || o1.getValue() == null || o2 == null || o2.getValue() == null) {
                    throw new IllegalArgumentException();
                }

                ComparableVersion v1 = new ComparableVersion(o1.getValue());
                ComparableVersion v2 = new ComparableVersion(o2.getValue());

                // we want to sort from newest to oldest
                return -(v1.compareTo(v2));
            }
        });

        // optionally add "special" versions
        if (Boolean.parseBoolean(form.getFirstValue("includeLatest", null))) {
            versions.add(0, new Option("LATEST", "LATEST"));
        }
        if (Boolean.parseBoolean(form.getFirstValue("includeRelease", null))) {
            versions.add(0, new Option("RELEASE", "RELEASE"));
        }

        // optionally limit the number of versions returned
        Integer limit;
        try {
            limit = Integer.parseInt(form.getFirstValue("l", null));
        } catch (NumberFormatException e) {
            limit = null;
        }
        if (limit != null && limit > 0 && versions.size() > limit) {
            versions = new ArrayList<Option>(versions.subList(0, limit));
        }

        return versions;
    }

    /**
     * @param options
     * @param value
     * @return true if the list of options contains an option with the given value
     */
    private boolean optionsContainsValue(List<Option> options, String value) {
        if (options == null) {
            return false;
        }
        for (Option option : options) {
            if (StringUtils.equals(value, option.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param artifact
     * @return String representation of the artifact version
     */
    private String buildOptionName(ArtifactInfo artifact) {
        StringBuilder name = new StringBuilder();
        name.append(artifact.version);
        name.append(" (");
        name.append(DateFormatUtils.ISO_DATETIME_FORMAT.format(artifact.lastModified));
        name.append(")");
        return name.toString();
    }

}
