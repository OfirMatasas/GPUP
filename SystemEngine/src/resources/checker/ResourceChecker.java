package resources.checker;

import myExceptions.*;
import resources.generated.GPUPConfiguration;
import resources.generated.GPUPDescriptor;
import resources.generated.GPUPTarget;
import resources.generated.GPUPTargetDependencies;
import target.Graph;
import target.Target;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ResourceChecker
{
    private enum DependencyType { requiredFor, dependsOn}
    private Graph graph;

    public Graph extractFromXMLToGraph(Path path) throws NotXMLFile, FileNotFound, DoubledTarget, InvalidConnectionBetweenTargets, EmptyGraph, InvalidTaskFound, DoublePricingForTask, TaskPricingNotFound {
        if(!path.getFileName().toString().endsWith(".xml"))
            throw new NotXMLFile(path.getFileName().toString());
        else if(!Files.isExecutable(path))
            throw new FileNotFound(path.getFileName().toString());

        //The file can be executed
        GPUPDescriptor descriptor = fromXmlFileToObject(path);
        this.graph = checkResource(descriptor);

        this.graph.calculatePositions();

        return this.graph;
    }

    private GPUPDescriptor fromXmlFileToObject(Path fileName)
    {
        GPUPDescriptor descriptor = null;
        try
        {
            File file = new File(fileName.toString());
            JAXBContext jaxbContext = JAXBContext.newInstance(GPUPDescriptor.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            descriptor = (GPUPDescriptor)jaxbUnmarshaller.unmarshal(file);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return descriptor;
    }

    private Graph checkResource(GPUPDescriptor descriptor) throws InvalidConnectionBetweenTargets, DoubledTarget, EmptyGraph,  InvalidTaskFound, DoublePricingForTask, TaskPricingNotFound {
        List<GPUPTarget> gpupTargetsAsList = descriptor.getGPUPTargets().getGPUPTarget();
        this.graph = FillTheGraphWithTargets(gpupTargetsAsList);
        this.graph.setGraphName(descriptor.getGPUPConfiguration().getGPUPGraphName());
        Target currentTarget, secondTarget;
        String currentTargetName, secondTargetName;

        if(descriptor.getGPUPTargets() == null || descriptor.getGPUPTargets().getGPUPTarget() == null)
            throw new EmptyGraph();

        for(GPUPTarget currentgpupTarget : gpupTargetsAsList)
        {
            currentTargetName = currentgpupTarget.getName();
            currentTarget = this.graph.getTarget(currentTargetName);
            currentTarget.setFQN(currentgpupTarget.getGPUPUserData());

            if(currentgpupTarget.getGPUPTargetDependencies() == null)
                continue;

            for(GPUPTargetDependencies.GPUGDependency dep :
                    currentgpupTarget.getGPUPTargetDependencies().getGPUGDependency())
            {
                //Check if the target exists in the graph
                secondTarget = this.graph.getTarget(dep.getValue());
                if(secondTarget == null)
                    throw new InvalidConnectionBetweenTargets(currentTargetName, dep.getValue());

                secondTargetName = secondTarget.getTargetName();

                if(!checkValidConnectionBetweenTwoTargets(currentTarget, secondTarget, dep.getType()))
                    throw new InvalidConnectionBetweenTargets(currentTargetName, secondTargetName);
            }

        }

        getPricingForTasks(descriptor);

        //Calculate all depends-on and all required-for for all targets
        this.graph.calculateAllDependsOn();
        this.graph.calculateAllRequiredFor();

        return this.graph;
    }

    private Graph FillTheGraphWithTargets(List<GPUPTarget> lst) throws DoubledTarget {
        Graph graph = new Graph();
        Target newTarget;

        for(GPUPTarget currentTarget : lst)
        {
            if(graph.getTarget(currentTarget.getName()) != null)
                throw new DoubledTarget(currentTarget.getName());

            newTarget = new Target();
            newTarget.setTargetName(currentTarget.getName());
            newTarget.setExtraInformation(currentTarget.getGPUPUserData());

            graph.addNewTargetToTheGraph(newTarget);
        }

        return graph;
    }

    private Boolean checkValidConnectionBetweenTwoTargets(Target target1, Target target2,String depType)
    {
        if(depType.equals(DependencyType.dependsOn.toString()))
        {
            if(target2.getDependsOnTargets().contains(target1))
                return false;
            else//Valid connection between the targets
            {
                target1.addToDependsOn(target2);
                target2.addToRequiredFor(target1);
            }
        }
        else // dep.getType().equals(DependencyType.DependsOn)
        {
            if(target2.getRequiredForTargets().contains(target1)) {
                return false;
            }
            else
            {
                target1.addToRequiredFor(target2);
                target2.addToDependsOn(target1);
            }
        }
        return true;
    }

    private void getPricingForTasks(GPUPDescriptor gpupDescriptor) throws TaskPricingNotFound, DoublePricingForTask, InvalidTaskFound {
       GPUPConfiguration.GPUPPricing gpupPricing =  gpupDescriptor.getGPUPConfiguration().getGPUPPricing();

       if(gpupPricing == null)
           throw new TaskPricingNotFound();

       for(GPUPConfiguration.GPUPPricing.GPUPTask curr : gpupPricing.getGPUPTask())
       {
           Graph.TaskType taskType = null;
           for(Graph.TaskType type : Graph.TaskType.values())
           {
               if(type.toString().equalsIgnoreCase(curr.getName())) {
                   taskType = type;
                   break;
               }
           }
           if(taskType == null)
               throw new InvalidTaskFound(curr.getName());
           else if(this.graph.getTasksPricesMap().containsKey(taskType))
               throw new DoublePricingForTask(curr.getName());

           //valid task type
           this.graph.addTaskAndPrice(taskType, curr.getPricePerTarget());
       }
    }
}