package com.example.application.views.movie;

import com.example.application.data.entity.Movie;
import com.example.application.data.service.MovieService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.util.UriUtils;

@PageTitle("Movie")
@Route(value = "Movie/:movieID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("admin")
public class MovieView extends Div implements BeforeEnterObserver {

    private final String MOVIE_ID = "movieID";
    private final String MOVIE_EDIT_ROUTE_TEMPLATE = "Movie/%d/edit";

    private Grid<Movie> grid = new Grid<>(Movie.class, false);

    private Upload image;
    private Image imagePreview;
    private TextField title;
    private TextField describe;
    private TextField duration;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Movie> binder;

    private Movie movie;

    private MovieService movieService;

    public MovieView(@Autowired MovieService movieService) {
        this.movieService = movieService;
        addClassNames("movie-view", "flex", "flex-col", "h-full");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        TemplateRenderer<Movie> imageRenderer = TemplateRenderer
                .<Movie>of("<img style='height: 64px' src='[[item.image]]' />").withProperty("image", Movie::getImage);
        grid.addColumn(imageRenderer).setHeader("Image").setWidth("68px").setFlexGrow(0);

        grid.addColumn("title").setAutoWidth(true);
        grid.addColumn("describe").setAutoWidth(true);
        grid.addColumn("duration").setAutoWidth(true);
        grid.setItems(query -> movieService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(MOVIE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MovieView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Movie.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.forField(duration).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("duration");

        binder.bindInstanceFields(this);

        attachImageUpload(image, imagePreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.movie == null) {
                    this.movie = new Movie();
                }
                binder.writeBean(this.movie);
                this.movie.setImage(imagePreview.getSrc());

                movieService.update(this.movie);
                clearForm();
                refreshGrid();
                Notification.show("Movie details stored.");
                UI.getCurrent().navigate(MovieView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the movie details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> movieId = event.getRouteParameters().getInteger(MOVIE_ID);
        if (movieId.isPresent()) {
            Optional<Movie> movieFromBackend = movieService.get(movieId.get());
            if (movieFromBackend.isPresent()) {
                populateForm(movieFromBackend.get());
            } else {
                Notification.show(String.format("The requested movie was not found, ID = %d", movieId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MovieView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        Label imageLabel = new Label("Image");
        imagePreview = new Image();
        imagePreview.setWidth("100%");
        image = new Upload();
        image.getStyle().set("box-sizing", "border-box");
        image.getElement().appendChild(imagePreview.getElement());
        title = new TextField("Title");
        describe = new TextField("Describe");
        duration = new TextField("Duration");
        Component[] fields = new Component[]{imageLabel, image, title, describe, duration};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            preview.setSrc(dataUrl);
            uploadBuffer.reset();
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Movie value) {
        this.movie = value;
        binder.readBean(this.movie);
        this.imagePreview.setVisible(value != null);
        if (value == null) {
            this.imagePreview.setSrc("");
        } else {
            this.imagePreview.setSrc(value.getImage());
        }

    }
}
