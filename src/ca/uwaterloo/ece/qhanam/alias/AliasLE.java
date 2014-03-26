/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,
 * Kevin Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
 *
 * This file is part of Crystal.
 *
 * Crystal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Crystal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Crystal.  If not, see <http://www.gnu.org/licenses/>.
 */
//package edu.cmu.cs.crystal.analysis.alias;
package ca.uwaterloo.ece.qhanam.alias;

import java.util.Collections;
import java.util.Set;


/**
 * This is an immutable lattice. 
 *
 */
public class AliasLE implements Aliasing {

        /** this is an immutable set. */
        private final Set<ObjectLabel> labels; 
        
        /** 
         * Create new lattice element with the given set of labels;
         * <b>do not mutate the given set after this call</b>. 
         * @param newLabels Label set that <b>must not be mutated</b> 
         * after this call.
         * @return new lattice element with the given set of labels.
         */
        public static AliasLE create(Set<ObjectLabel> newLabels) {
                return new AliasLE(newLabels);
        }
        
        /**
         * Create new lattice element with only the given label
         * in the label set.
         * @param label Object label.
         * @return new lattice element with only the given label.
         */
        public static AliasLE create(ObjectLabel label) {
                return new AliasLE(label);
        }
        
        /**
         * Returns a bottom lattice element.
         * @return bottom lattice element.
         */
        public static AliasLE bottom() {
                return new AliasLE();
        }
        
        /**
         * Creates lattice element with empty label set.
         */
        protected AliasLE() {
                this.labels = Collections.emptySet();
        }
        
        /**
         * Creates lattice element with singleton label set.
         * @param label The singleton label to be placed in the set.
         */
        protected AliasLE(ObjectLabel label) {
                this.labels = Collections.singleton(label);
        }

        /**
         * Creates lattice element with the given set;
         * <b>do not mutate the given set after this call</b>.
         * @param labels Label set <b>not to be modified</b> after this call.
         */
        protected AliasLE(Set<ObjectLabel> labels) {
                this.labels = Collections.unmodifiableSet(labels);
        }

        public Set<ObjectLabel> getLabels() {
                return labels;
        } 
        
        public boolean hasAnyLabels(Set<ObjectLabel> labelsToFind) {
                for (ObjectLabel label : labelsToFind)
                        if (labels.contains(label))
                                return true;
                return false;
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((labels == null) ? 0 : labels.hashCode());
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
                
                final AliasLE other = (AliasLE) obj;
                return labels.containsAll(other.labels) && other.labels.containsAll(labels);
        }
        
        
        public String toString() {
                return labels.toString();
        }

}