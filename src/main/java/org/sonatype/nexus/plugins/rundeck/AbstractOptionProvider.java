/*
 * Copyright 2011 Vincent Behar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.nexus.plugins.rundeck;

import java.io.Serializable;
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

    /**
     * JavaBean representation of a RunDeck option
     */
    public static class Option implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        private final String value;

        public Option(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Option other = (Option) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Option [name=" + name + ", value=" + value + "]";
        }
    }

}
