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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;


import edu.cmu.cs.crystal.ICrystalAnalysis;
import edu.cmu.cs.crystal.analysis.metrics.LoopCounter;
import edu.cmu.cs.crystal.flow.ILabel;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.AbstractTACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.AssignmentInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.DotClassInstruction;
import edu.cmu.cs.crystal.tac.model.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.UnaryOperation;
import edu.cmu.cs.crystal.tac.model.Variable;

public class MayAliasTransferFunction extends
                AbstractTACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, AliasLE>> {

        private Map<Variable, ObjectLabel> labelContext;
        private final TupleLatticeOperations<Variable, AliasLE> ops =
                new TupleLatticeOperations<Variable, AliasLE>(
                                SingleObjectAliasOps.getAliasOps(),new AliasLE());

        private LoopCounter loopCounter;
        
        public MayAliasTransferFunction(ICrystalAnalysis analysis) {
                labelContext = new HashMap<Variable, ObjectLabel>();
                loopCounter = new LoopCounter();
        }
        
        public ILatticeOperations<TupleLatticeElement<Variable, AliasLE>> getLatticeOperations() {
                return ops;
        }
        
        public TupleLatticeElement<Variable, AliasLE> createEntryValue(MethodDeclaration m) {
                
                TupleLatticeElement<Variable, AliasLE> entry = ops.getDefault();
                Variable thisVar = this.getAnalysisContext().getThisVariable();
                
                if (thisVar != null)
                        entry.put(thisVar, AliasLE.create(new DefaultObjectLabel(thisVar.resolveType(), false)));
                
                return entry;
        }

        private ObjectLabel getLabel(Variable associatedVar, ITypeBinding binding, TACInstruction declaringInstr) {
                //if we already produced an initial label for this variable, use it
                if (labelContext.get(associatedVar) != null) {
                        return labelContext.get(associatedVar);
                }
                else {
                        boolean isInLoop = loopCounter.isInLoop(declaringInstr.getNode());
                        ObjectLabel label = new DefaultObjectLabel(binding, isInLoop);
                        labelContext.put(associatedVar, label);
                        return label;
                }
        }
        
        private TupleLatticeElement<Variable, AliasLE> 
        putSingletonLabel(AssignmentInstruction instr, TupleLatticeElement<Variable, AliasLE> value) {
                ObjectLabel label = getLabel(instr.getTarget(), instr.getTarget().resolveType(), instr);
                AliasLE aliases = createSingletonLE(label);
                value.put(instr.getTarget(), aliases);
                return value;
        }
        
        private static AliasLE createSingletonLE(ObjectLabel label) {
                AliasLE result = AliasLE.create(label);
                return result;
        }
        
        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        ArrayInitInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        BinaryOperation binop, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                return LabeledSingleResult.createResult(putSingletonLabel(binop, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        CastInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                // TODO maybe one could use the type cased to to drop aliases of incompatible types?
                value.put(instr.getTarget(), value.get(instr.getOperand()));
                return LabeledSingleResult.createResult(value, labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        DotClassInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                //TODO: consider how to handle literals
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        CopyInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                //if value does not contain instr, then what? will it do the put automatically?
                value.put(instr.getTarget(), value.get(instr.getOperand()));
                return LabeledSingleResult.createResult(value, labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        InstanceofInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                // result of instanceof is a fresh (boolean) label
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        LoadLiteralInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                //TODO: consider how to handle literals
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        LoadArrayInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                //TODO: let this alias anything of the right type
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        LoadFieldInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                //TODO: let this alias anything of the right type
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        MethodCallInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                //TODO: let this alias anything of the right type
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        NewArrayInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        NewObjectInstruction instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        UnaryOperation unop, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                return LabeledSingleResult.createResult(putSingletonLabel(unop, value), labels);
        }

        public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
                        SourceVariableDeclaration instr, List<ILabel> labels,
                        TupleLatticeElement<Variable, AliasLE> value) {
                if (instr.isFormalParameter()) {
                        ObjectLabel label = getLabel(instr.getDeclaredVariable(), instr.resolveBinding().getType(), instr);
                        AliasLE aliases = createSingletonLE(label);
                        value.put(instr.getDeclaredVariable(), aliases);
                }
                return LabeledSingleResult.createResult(value, labels);
        }
}
