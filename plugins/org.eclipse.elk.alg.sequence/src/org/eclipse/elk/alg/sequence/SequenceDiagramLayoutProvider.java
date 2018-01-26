/*******************************************************************************
 * Copyright (c) 2018 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kiel University - initial API and implementation
 *******************************************************************************/
package org.eclipse.elk.alg.sequence;

import java.util.List;

import org.eclipse.elk.alg.sequence.graph.LayoutContext;
import org.eclipse.elk.alg.sequence.graph.transform.ElkGraphTransformer;
import org.eclipse.elk.alg.sequence.graph.transform.IGraphTransformer;
import org.eclipse.elk.alg.sequence.options.CoordinateAssignmentStrategy;
import org.eclipse.elk.alg.sequence.options.CycleBreakingStrategy;
import org.eclipse.elk.alg.sequence.options.MessageLayeringStrategy;
import org.eclipse.elk.alg.sequence.options.SequenceDiagramOptions;
import org.eclipse.elk.alg.sequence.options.SpaceAllocationStrategy;
import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.UnsupportedGraphException;
import org.eclipse.elk.core.alg.AlgorithmAssembler;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkNode;

/**
 * Layout algorithm for Papyrus sequence diagrams.
 * 
 * @author grh
 */
public final class SequenceDiagramLayoutProvider extends AbstractLayoutProvider {
    
    /** The algorithm assembler we use to assemble our algorithm configurations. */
    private final AlgorithmAssembler<SequencePhases, LayoutContext> algorithmAssembler =
            AlgorithmAssembler.<SequencePhases, LayoutContext>create(SequencePhases.class);
    

    @Override
    public void layout(final ElkNode parentNode, final IElkProgressMonitor progressMonitor) {
        // Prevent the surrounding diagram from being laid out
        if (parentNode.getParent() == null) {
            throw new UnsupportedGraphException("Sequence diagram layout can only be run on surrounding interactions.");
        }
        
        // Import the graph
        IGraphTransformer<ElkNode> graphTransformer = new ElkGraphTransformer();
        LayoutContext context = graphTransformer.importGraph(parentNode);

        // Assemble and execute the algorithm
        List<ILayoutProcessor<LayoutContext>> algorithm = assembleLayoutProcessors(context);
        
        progressMonitor.begin("Sequence Diagram Layouter", algorithm.size());
        for (ILayoutProcessor<LayoutContext> processor : algorithm) {
            processor.process(context, progressMonitor.subTask(1));
        }
        progressMonitor.done();
        
        // Apply the layout
        graphTransformer.applyLayout(context);
    }
    
    /**
     * Assembles the list of layout processors that, when run in order, implement the sequence
     * diagram layout algorithm. The list may be different based on the given layout context.
     * 
     * @param context
     *            the layout context that contains all relevant information for the current layout
     *            run.
     * @return list of layout processors.
     */
    private List<ILayoutProcessor<LayoutContext>> assembleLayoutProcessors(final LayoutContext context) {
        // Setup the algorithm assembler
        algorithmAssembler.reset();
        
        algorithmAssembler.setPhase(SequencePhases.P1_SPACE_ALLOCATION,
                SpaceAllocationStrategy.DEFAULT);
        algorithmAssembler.setPhase(SequencePhases.P2_CYCLE_BREAKING,
                CycleBreakingStrategy.DEFAULT);
        algorithmAssembler.setPhase(SequencePhases.P3_MESSAGE_LAYERING,
                MessageLayeringStrategy.DEFAULT);
        algorithmAssembler.setPhase(SequencePhases.P4_LIFELINE_SORTING,
                context.kgraph.getProperty(SequenceDiagramOptions.LIFELINE_SORTING_STRATEGY));
        algorithmAssembler.setPhase(SequencePhases.P5_COORDINATE_ASSIGNMENT,
                CoordinateAssignmentStrategy.DEFAULT);
        
        return algorithmAssembler.build(context);
    }
    
}