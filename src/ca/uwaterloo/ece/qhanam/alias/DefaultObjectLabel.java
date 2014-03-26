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

import org.eclipse.jdt.core.dom.ITypeBinding;


public class DefaultObjectLabel implements ObjectLabel {
        private static int LABELINDEX = 0;
        private int label;
        private ITypeBinding type;
        private boolean isSummary;
        
        public DefaultObjectLabel(ITypeBinding type, boolean isSummaryLabel) {
                label = ++LABELINDEX;
                this.isSummary = isSummaryLabel;
                this.type = type;
        }

        /* (non-Javadoc)
         * @see edu.cmu.cs.crystal.analysis.alias.ObjectLabel#isSummary()
         */
        public boolean isSummary() {return isSummary;}
        
        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + label;
                return result;
        }

        public String toString() {return "L" + Integer.toString(label);}
        
        @Override
        public boolean equals(Object obj) {
                if (this == obj)
                        return true;
                if (obj == null)
                        return false;
                if (getClass() != obj.getClass())
                        return false;
                final DefaultObjectLabel other = (DefaultObjectLabel) obj;
                return label == other.label;
        }

        
        /* (non-Javadoc)
         * @see edu.cmu.cs.crystal.analysis.alias.ObjectLabel#getType()
         */
        public ITypeBinding getType() {
                return type;
        }
}
