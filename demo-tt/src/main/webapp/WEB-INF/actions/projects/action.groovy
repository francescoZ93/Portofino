import com.manydesigns.elements.Mode
import com.manydesigns.elements.forms.Form
import com.manydesigns.portofino.resourceactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.tt.Refresh
import com.manydesigns.portofino.tt.TtUtils
import org.apache.shiro.SecurityUtils
import org.springframework.beans.factory.annotation.Autowired

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsCrudAction extends CrudAction {

    @Autowired
    Refresh refresh

    Object old

    static {
        logger.info("Loaded action - ${ProjectsCrudAction.class.hashCode()} - ${Refresh.class.hashCode()}")
    }

    //**************************************************************************
    // Role checking
    //**************************************************************************

    public boolean isContributor() {
        return TtUtils.principalHasProjectRole(object, TtUtils.ROLE_CONTRIBUTOR);
    }

    public boolean isEditor() {
        return TtUtils.principalHasProjectRole(object, TtUtils.ROLE_EDITOR);
    }

    public boolean isManager() {
        return TtUtils.principalHasProjectRole(object, TtUtils.ROLE_MANAGER);
    }

    @Override
    boolean isBulkOperationsEnabled() {
        false //Remember, if you set it to true, you also have to disable the editPostProcess logic or bulk updates won't work.
    }

    //**************************************************************************
    // Extension hooks
    //**************************************************************************

    @Override
    protected void createSetup(Object object) {
        object.last_ticket = 0L;
    }

    @Override
    protected boolean createValidate(Object object) {
        Date now = new Date();
        object.created = now;
        object.last_updated = now;
        return true;
    }

    @Override
    protected void createPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_PROJECT_CREATED,
                null,
                null,
                object,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Map member = new HashMap();
        member.project = object.id;
        member.user_ = principal.id;
        member.role = TtUtils.ROLE_MANAGER;
        member.notifications = true;
        session.save("members", (Object)member);

        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_MEMBER_CREATED,
                null,
                principal,
                object,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

    }

//    protected Resolution getSuccessfulSaveView() {
//        return new RedirectResolution(context.getActionPath() + "/" + object.id);
//    }
//
//    //**************************************************************************
//    // Edit customizations
//    //**************************************************************************
//    @Override
//    @Buttons([
//        @Button(list = "crud-read", key = "edit", order = 1d, icon = "glyphicon-edit white",
//                group = "crud", type = Button.TYPE_SUCCESS),
//        @Button(list = "crud-read-default-button", key = "search")
//    ])
//    @Guard(test="isManager()", type=GuardType.VISIBLE)
//    Resolution edit() {
//        return super.edit()
//    }
//
//    @Override
//    @Button(list = "crud-edit", key = "update", order = 1d, type = Button.TYPE_PRIMARY)
//    @Guard(test="isManager()", type=GuardType.VISIBLE)
//    Resolution update() {
//        Date now = new Date();
//        object.last_updated = now;
//        return super.update()
//    }

    @Override
    protected boolean editValidate(Object object) {
        Date now = new Date();
        object.last_updated = now;
        return true;
    }

    protected void editSetup(Object object) {
        old = object.clone();
    }


    @Override
    protected void editPostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Form newForm = form;
        setupForm(Mode.EDIT);
        form.readFromObject(old);
        String message = TtUtils.createDiffMessage(form, newForm);
        if (message != null) {
            Date now = new Date();
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_PROJECT_UPDATED,
                    message,
                    null,
                    object,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    //**************************************************************************
    // Delete customizations
    //**************************************************************************


//    @Button(list = "crud-read", key = "delete", order = 2d, icon = Button.ICON_TRASH)
//    @Guard(test = "isManager()", type = GuardType.VISIBLE)
//    public Resolution delete() {
//        return super.delete();
//    }

    @Override
    protected void deletePostProcess(Object object) {
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session,
                principal,
                now,
                TtUtils.ACTIVITY_TYPE_PROJECT_DELETED,
                null,
                null,
                object,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    protected void doDelete(Object object) {
        session.createQuery('delete from members where project = :project')
                .setParameter('project', object.id)
                .executeUpdate()
        super.doDelete(object)
    }

}
