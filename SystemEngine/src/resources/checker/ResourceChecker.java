package resources.checker;

import myExceptions.*;
import resources.generated.GPUPDescriptor;
import resources.generated.GPUPTarget;
import resources.generated.GPUPTargetDependencies;
import target.Graph;
import target.Target;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ResourceChecker
{
    private String workingDirectoryString;

    private enum DependencyType { requiredFor, dependsOn};

    public Graph extractFromXMLToGraph(Path path) throws NotXMLFile, FileNotFound, DoubledTarget, InvalidConnectionBetweenTargets, EmptyGraph, IOException, NotDirectory, InvalidThreadsNumber, NotUniqueSerialName, TargetNotExisted {
        if(!path.getFileName().toString().endsWith(".xml"))
            throw new NotXMLFile(path.getFileName().toString());
        else if(!Files.isExecutable(path))
            throw new FileNotFound(path.getFileName().toString());

        //The file can be executed
        GPUPDescriptor descriptor = fromXmlFileToObject(path);
        Graph graph = checkResource(descriptor);

        graph.calculatePositions();

        return graph;
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

    private Graph checkResource(GPUPDescriptor descriptor) throws InvalidConnectionBetweenTargets, DoubledTarget, EmptyGraph, IOException, NotDirectory, InvalidThreadsNumber, NotUniqueSerialName, TargetNotExisted {
        List<GPUPTarget> gpupTargetsAsList = descriptor.getGPUPTargets().getGPUPTarget();
        Graph graph = FillTheGraphWithTargets(gpupTargetsAsList);
        graph.setGraphName(descriptor.getGPUPConfiguration().getGPUPGraphName());
        Target currentTarget, secondTarget;
        String currentTargetName, secondTargetName;

        setWorkingDirectory(descriptor);

        if(descriptor.getGPUPTargets() == null || descriptor.getGPUPTargets().getGPUPTarget() == null)
            throw new EmptyGraph();

        if(descriptor.getGPUPConfiguration().getGPUPMaxParallelism() < 1)
            throw new InvalidThreadsNumber();
        graph.setParallelThreads(descriptor.getGPUPConfiguration().getGPUPMaxParallelism());

        for(GPUPTarget currentgpupTarget : gpupTargetsAsList)
        {
            currentTargetName = currentgpupTarget.getName();
            currentTarget = graph.getTarget(currentTargetName);

            if(currentgpupTarget.getGPUPTargetDependencies() == null)
                continue;

            for(GPUPTargetDependencies.GPUGDependency dep :
                    currentgpupTarget.getGPUPTargetDependencies().getGPUGDependency())
            {
                //Check if the target exists in the graph
                secondTarget = graph.getTarget(dep.getValue());
                if(secondTarget == null)
                    throw new InvalidConnectionBetweenTargets(currentTargetName, dep.getValue());

                secondTargetName = secondTarget.getTargetName();

                if(!checkValidConnectionBetweenTwoTargets(currentTarget, secondTarget, dep.getType()))
                    throw new InvalidConnectionBetweenTargets(currentTargetName, secondTargetName);
            }
        }

        //Get all serial sets names and fill them up with targets' names
        setAllSerialSets(descriptor, graph);

        //Calculate all depends-on and all required-for for all targets
        graph.calculateAllDependsOn();
        graph.calculateAllRequiredFor();

        return graph;
    }

    private void setAllSerialSets(GPUPDescriptor descriptor, Graph graph) throws NotUniqueSerialName, TargetNotExisted {
        if (descriptor.getGPUPSerialSets() != null)
        {
            graph.setSerialSetsNames(GetAllSerialSetsNames(descriptor.getGPUPSerialSets()));
            graph.setSerialSetsMap(ConnectAllTargetsToSerialSets(descriptor.getGPUPSerialSets(), graph));
        }
    }

    private void setWorkingDirectory(GPUPDescriptor descriptor) throws NotDirectory, IOException {
        workingDirectoryString = descriptor.getGPUPConfiguration().getGPUPWorkingDirectory();
        Path workingDirectoryPath = new File(workingDirectoryString).toPath();

        if(!Files.exists(workingDirectoryPath))
            Files.createDirectories(workingDirectoryPath);
        else if(!Files.isDirectory(workingDirectoryPath))
            throw new NotDirectory(workingDirectoryString);
    }

    private Set<String> GetAllSerialSetsNames(GPUPDescriptor.GPUPSerialSets gpupSerialSets) throws NotUniqueSerialName {
        Set<String> serialSetsNamesSet = new HashSet();
        String currentSerialSetName;

        for(GPUPDescriptor.GPUPSerialSets.GPUPSerialSet currentSerialSet : gpupSerialSets.getGPUPSerialSet())
        {
            currentSerialSetName = currentSerialSet.getName();

            if(serialSetsNamesSet.contains(currentSerialSetName))
                throw new NotUniqueSerialName(currentSerialSetName);

            serialSetsNamesSet.add(currentSerialSetName);
        }
        return serialSetsNamesSet;
    }

    private Map<String, Set<String>> ConnectAllTargetsToSerialSets(GPUPDescriptor.GPUPSerialSets gpupSerialSets, Graph graph) throws TargetNotExisted {
        Map<String, Set<String>> mappedSerialSets = new HashMap<>();
        Set<String> currentSerialSet;
        String currentSerialSetName;
        String[] targets;

        for(GPUPDescriptor.GPUPSerialSets.GPUPSerialSet currentGPUPSerialSet : gpupSerialSets.getGPUPSerialSet())
        {
            currentSerialSet = new HashSet<>();
            targets = currentGPUPSerialSet.getTargets().split(",");
            currentSerialSetName = currentGPUPSerialSet.getName();

            for(String currentTargetName : targets)
            {
                if (currentSerialSet.contains(currentTargetName))
                    continue;
                else if(!graph.getGraphTargets().containsKey(currentTargetName.toLowerCase()))
                    throw new TargetNotExisted(currentTargetName, currentSerialSetName);

                currentSerialSet.add(currentTargetName);
                graph.getTarget(currentTargetName).addSerialSet(currentSerialSetName);
            }
            mappedSerialSets.put(currentSerialSetName, currentSerialSet);
        }
        return mappedSerialSets;
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
        String target1Name = target1.getTargetName(), target2Name = target2.getTargetName();

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

    public String getWorkingDirectoryPath()
    {
        return workingDirectoryString;
    }
}