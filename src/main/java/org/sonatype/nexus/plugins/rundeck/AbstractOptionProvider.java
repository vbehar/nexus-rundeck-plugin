/**
 * $URL$
 *
 * $LastChangedBy$ - $LastChangedDate$
 */
package org.sonatype.nexus.plugins.rundeck;

import java.util.Arrays;
import java.util.List;
import org.restlet.data.MediaType;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

/**
 * Option provider for RunDeck - see http://rundeck.org/docs/RunDeck-Guide.html#option-model-provider<br>
 * Abstract class for writing option providers.
 * 
 * @author Vincent Behar
 */
public abstract class AbstractOptionProvider extends AbstractPlexusResource {

    @Override
    public List<Variant> getVariants() {
        return Arrays.asList(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    public PathProtectionDescriptor getResourceProtection() {
        // should be new PathProtectionDescriptor(this.getResourceUri(), "anon");
        // BUT https://issues.sonatype.org/browse/NEXUS-3951
        return new PathProtectionDescriptor(getResourceUri(), "authcBasic");
    }

    @Override
    public Object getPayloadInstance() {
        return null;
    }

}
