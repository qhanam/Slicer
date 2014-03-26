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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;


import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.ITACFlowAnalysis;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.IAnalysisReporter;

public class MayAliasAnalysis extends AbstractCrystalMethodAnalysis {
        
        private static final Logger log = Logger.getLogger(MayAliasAnalysis.class.getName());
        
        private ITACFlowAnalysis<TupleLatticeElement<Variable, AliasLE>> fa;
        
        /**
         * This visitor will only be called if log level is {@link Level#FINE} or lower.
         */
        private ASTVisitor checkResults = new ASTVisitor() {
                @Override
                public void postVisit(ASTNode node) {
                        if (node instanceof Statement) {
                                TupleLatticeElement<Variable, AliasLE> le = fa.getResultsAfter(node);
                                if (le == null)
                                        return;
                                if(log.isLoggable(Level.FINE)) {
                                        StringBuffer msg = new StringBuffer();
                                        msg.append("Aliases at the end of ");
                                        msg.append(node.toString());
                                        for (Variable var : le.getKeySet()) {
                                                AliasLE aliases = le.get(var);
                                                msg.append('\n');
                                                msg.append(var.getSourceString() + "(" + var.toString() + "):\n");
                                                msg.append(aliases.getLabels());
                                        }
                                        log.fine(msg.toString());
                                }
                        }
                }
        };
        
        public MayAliasAnalysis(IAnalysisReporter reporter, IAnalysisInput analysisInput) {
        	super();	
        	this.reporter = reporter;
        	this.analysisInput = analysisInput;
        	Logger log = LogManager.getLogManager().getLogger(MayAliasAnalysis.class.getName());
        	log.setLevel(Level.FINE);
        	ConsoleHandler handler = new ConsoleHandler();
        	handler.setFormatter(new SimpleFormatter());
        	handler.setLevel(Level.ALL);
        	log.addHandler(handler);
        }
        
        
        /**
         * Get the aliases of a variable at a particular node. Returns the aliases
         * after the given node has been evaluated.
         * @param var The variable to look for
         * @param node The ASTNode that we just evaluated
         * @return A set of possible aliases, including temporary variables.
         */
        public Set<Variable> getAfterAliases(Variable var, ASTNode node) {
                return getAliases(fa.getResultsAfter(node), var);
        }
        
        /**
         * Get the aliases of a variable at a particular node. Returns the aliases
         * after the given node has been evaluated.
         * @param var The variable to look for
         * @param node The ASTNode that we just evaluated
         * @return A set of possible aliases, including temporary variables.
         */
        public Set<Variable> getBeforeAliases(Variable var, ASTNode node) {
                return getAliases(fa.getResultsBefore(node), var);
        }
        
        public Set<ObjectLabel> getBeforeAliasLabels(Variable var, ASTNode node) {
                return fa.getResultsBefore(node).get(var).getLabels();
        }
        
        public Set<ObjectLabel> getAfterAliasLabels(Variable var, ASTNode node) {
                TupleLatticeElement<Variable, AliasLE> le = fa.getResultsAfter(node);
                return le.get(var).getLabels();
        }
        
        /**
         * Get all the object labels at this node which have the given type, regardless
         * of who the alias is. This will also include anything which is a subtype of
         * the given type.
         * This is expensive.
         * @param typeBinding
         * @param node
         * @return A set of all object labels at node which are castable to typeBinding
         */
        public Set<ObjectLabel> getBeforeAliasLabels(ITypeBinding typeBinding,
                        ASTNode node) {
                return getAliasLabels(typeBinding, fa.getResultsBefore(node));
        }
        
        public Set<ObjectLabel> getBeforeAliasLabels(String typeName, ASTNode node) {
                return getAliasLabels(typeName, fa.getResultsBefore(node));
        }

        public Set<ObjectLabel> getAfterAliasLabels(String typeName, ASTNode node) {
                return getAliasLabels(typeName, fa.getResultsAfter(node));
        }

        public Set<ObjectLabel> getAfterAliasLabels(ITypeBinding typeBinding,
                        ASTNode node) {
                return getAliasLabels(typeBinding, fa.getResultsAfter(node));
        }
        
        private Set<ObjectLabel> getAliasLabels(String typeName, TupleLatticeElement<Variable, AliasLE> le) {
                Set<ObjectLabel> labels = new HashSet<ObjectLabel>();
                for (Variable var : le.getKeySet()) {
                        Set<ObjectLabel> aliases = le.get(var).getLabels();
                        
                        for (ObjectLabel label : aliases) {
                                //TODO: fix this bug!!
                                if (typeName.equals(label.getType().getQualifiedName()))
                                        labels.add(label);
                        }
                }
                return labels;

        }
        
        private Set<ObjectLabel> getAliasLabels(ITypeBinding typeBinding, TupleLatticeElement<Variable, AliasLE> le) {
                //TODO: Find a cheaper way of maintaining this context and have the ability
                //to query it at any node. Maybe a Contextual Lattice?
                Set<ObjectLabel> labels = new HashSet<ObjectLabel>();
                for (Variable var : le.getKeySet()) {
                        Set<ObjectLabel> aliases = le.get(var).getLabels();
                        
                        for (ObjectLabel label : aliases) {
                                if (typeBinding.isCastCompatible(label.getType()))
                                        labels.add(label);
                        }
                }
                return labels;
        }
        
        @Override
        public void analyzeMethod(MethodDeclaration d) {
                MayAliasTransferFunction tf = new MayAliasTransferFunction(this);
                fa = new TACFlowAnalysis<TupleLatticeElement<Variable, AliasLE>>(tf, 
                                this.analysisInput.getComUnitTACs().unwrap());
                
                // must call getResultsAfter at least once on this method,
                // or the analysis won't be run on this method
                fa.getResultsAfter(d);
                if(log.isLoggable(Level.FINE))
                        d.accept(checkResults); 
        }
        
/*      private void printLattice(TupleLatticeElement<Variable, AliasLE> lattice) {
                for (Variable var : lattice.getKeySet()) {
                        if (!(var instanceof SourceVariable))
                                continue;
                        reporter.debugOut().println(var.getSourceString() + ":");
                        
                        for (Variable alias : getAliases(lattice, var)) {
                                reporter.debugOut().println("   " + alias.getSourceString() + "(" + alias.toString() + ")");
                        }
                }               
        }
*/      
        private Set<Variable> getAliases(TupleLatticeElement<Variable, AliasLE> tuple, Variable varToFind) {
                Set<ObjectLabel> labels = tuple.get(varToFind).getLabels();
                Set<Variable> aliases = new HashSet<Variable>();
                for (Variable var : tuple.getKeySet()) {
                        if (var != varToFind && tuple.get(var).hasAnyLabels(labels))
                                aliases.add(var);
                }
                return aliases;
        }

        public TupleLatticeElement<Variable, AliasLE> getResultsAfter(TACInstruction instr) {
                return fa.getResultsAfter(instr.getNode());
        }
        
        public Set<ObjectLabel> getAllLabelsBefore(TACInstruction instr) {
                return getAllLabels(fa.getResultsBefore(instr.getNode()));
        }
        
        public Set<ObjectLabel> getAllLabelsAfter(TACInstruction instr) {
                return getAllLabels(fa.getResultsAfter(instr.getNode()));
        }
        
        private Set<ObjectLabel> getAllLabels(TupleLatticeElement<Variable, AliasLE> lattice) {
                Set<ObjectLabel> allLabels = new HashSet<ObjectLabel>();
                
                for (Variable var : lattice.getKeySet()) {
                        AliasLE aliases = lattice.get(var);
                        allLabels.addAll(aliases.getLabels());
                }
                
                return allLabels;
        }

        
        public Variable getThisVar(MethodDeclaration methodDecl) {
                return fa.getThisVariable(methodDecl);
        }

        public TupleLatticeElement<Variable, AliasLE> getResultsBefore(
                        TACInstruction instr) {
                return fa.getResultsBefore(instr);
        }

}