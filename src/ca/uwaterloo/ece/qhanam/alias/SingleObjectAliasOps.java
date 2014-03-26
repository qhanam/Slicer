/**
 * Copyright (c) 2006-2009 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,    
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

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;


import edu.cmu.cs.crystal.flow.ILatticeOperations;

/**
 * Lattice operations for {@link AliasLE}.
 * @author Kevin Bierhoff
 * @since Crystal 3.4.1
 */
public class SingleObjectAliasOps implements ILatticeOperations<AliasLE> {
        
        /** Holds the singleton instance of this class. */
        private static SingleObjectAliasOps ALIAS_OPS;

        /**
         * Returns the singleton instance of this class.
         * @return the singleton instance of this class.
         */
        public static SingleObjectAliasOps getAliasOps() {
                if(ALIAS_OPS == null)
                        ALIAS_OPS = new SingleObjectAliasOps();
                return ALIAS_OPS;
        }
        
        /**
         * Protected constructor for singleton pattern.
         */
        protected SingleObjectAliasOps() {
                super();
        }

        public boolean atLeastAsPrecise(AliasLE info, AliasLE reference,
                        ASTNode node) {
                return reference.getLabels().containsAll(info.getLabels());
        }

        public AliasLE bottom() {
                return AliasLE.bottom();
        }

        public AliasLE copy(AliasLE original) {
                return original;
        }

        public AliasLE join(AliasLE someInfo, AliasLE otherInfo, ASTNode node) {
                HashSet<ObjectLabel> copy;
                copy = new HashSet<ObjectLabel>(someInfo.getLabels());
                copy.addAll(otherInfo.getLabels());
                return new AliasLE(copy);
        }

}