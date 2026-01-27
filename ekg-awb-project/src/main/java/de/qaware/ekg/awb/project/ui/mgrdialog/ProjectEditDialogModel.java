package de.qaware.ekg.awb.project.ui.mgrdialog;

import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * A specialized version of the {@link ProjectDialogModel} that stores
 * additional properties for the the project edition use-cases.
 */
public class ProjectEditDialogModel extends ProjectDialogModel {

    private Project editedProject = null;

    private BooleanProperty hasImporterSourceRepository = new SimpleBooleanProperty(false);


    //=================================================================================================================
    // Accessor API of the CreateProjectDialogModel
    //=================================================================================================================

    @Override
    public void setSelectedRepository(EkgRepository repository) {
        super.setSelectedRepository(repository);
    }

    public void setEditedProject(Project project) {
        this.editedProject = project;

        if (editedProject != null) {
            hasImporterSourceRepository.set(editedProject.useSplitSource());
        }
    }

    public Project getProject() {
        return editedProject;
    }

    public BooleanProperty hasImporterSourceRepositoryProperty() {
        return hasImporterSourceRepository;
    }

    protected BooleanBinding isValidProjectNameProperty() {
        return new BooleanBinding() {

            {
                bind(projectName, projectNameUnique);
            }

            @Override
            protected boolean computeValue() {
                return StringUtils.isNotBlank(getProjectName())
                        && !projectNameUnique.get();
            }
        };
    }
}
