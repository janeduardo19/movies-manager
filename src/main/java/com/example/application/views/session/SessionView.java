package com.example.application.views.session;

import com.example.application.data.entity.Session;
import com.example.application.data.service.SessionService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Session")
@Route(value = "Session/:sessionID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class SessionView extends Div implements BeforeEnterObserver {

    private final String SESSION_ID = "sessionID";
    private final String SESSION_EDIT_ROUTE_TEMPLATE = "Session/%d/edit";

    private Grid<Session> grid = new Grid<>(Session.class, false);

    private DatePicker date;
    private DateTimePicker iniTime;
    private DateTimePicker endTime;
    private TextField value;
    private TextField animation;
    private TextField audio;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Session> binder;

    private Session session;

    private SessionService sessionService;

    public SessionView(@Autowired SessionService sessionService) {
        this.sessionService = sessionService;
        addClassNames("session-view", "flex", "flex-col", "h-full");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("date").setAutoWidth(true);
        grid.addColumn("iniTime").setAutoWidth(true);
        grid.addColumn("endTime").setAutoWidth(true);
        grid.addColumn("value").setAutoWidth(true);
        grid.addColumn("animation").setAutoWidth(true);
        grid.addColumn("audio").setAutoWidth(true);
        grid.setItems(query -> sessionService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SESSION_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SessionView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Session.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.forField(value).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("value");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.session == null) {
                    this.session = new Session();
                }
                binder.writeBean(this.session);

                sessionService.update(this.session);
                clearForm();
                refreshGrid();
                Notification.show("Session details stored.");
                UI.getCurrent().navigate(SessionView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the session details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> sessionId = event.getRouteParameters().getInteger(SESSION_ID);
        if (sessionId.isPresent()) {
            Optional<Session> sessionFromBackend = sessionService.get(sessionId.get());
            if (sessionFromBackend.isPresent()) {
                populateForm(sessionFromBackend.get());
            } else {
                Notification.show(String.format("The requested session was not found, ID = %d", sessionId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(SessionView.class);
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
        date = new DatePicker("Date");
        iniTime = new DateTimePicker("Ini Time");
        iniTime.setStep(Duration.ofSeconds(1));
        endTime = new DateTimePicker("End Time");
        endTime.setStep(Duration.ofSeconds(1));
        value = new TextField("Value");
        animation = new TextField("Animation");
        audio = new TextField("Audio");
        Component[] fields = new Component[]{date, iniTime, endTime, value, animation, audio};

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

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Session value) {
        this.session = value;
        binder.readBean(this.session);

    }
}
