package seedu.address.logic.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_COLOR_BLUE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_B;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showApparelAtIndex;
import static seedu.address.logic.commands.CommandTestUtilExtra.DESC_CONNY;
import static seedu.address.logic.commands.CommandTestUtilExtra.DESC_DENDI;
import static seedu.address.testutil.TypicalApparels.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_APPAREL;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_APPAREL;

import org.junit.Test;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.UnavailableCommand.UnavailablePersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.apparel.Apparel;
import seedu.address.testutil.ApparelBuilder;
import seedu.address.testutil.UnavailableApparelDescriptorBuilder;

/**
 * Contains integration tests (interaction with the Model, UndoCommand and RedoCommand) and unit tests for EditCommand.
 */
public class UnavailableCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private CommandHistory commandHistory = new CommandHistory();

    @Test
    public void execute_dirty_success() {
        Apparel editedApparel = new ApparelBuilder().buildUnavailable();
        editedApparel.use();
        UnavailablePersonDescriptor descriptor = new UnavailableApparelDescriptorBuilder(editedApparel).build();
        UnavailableCommand unavailableCommand = new UnavailableCommand(INDEX_FIRST_APPAREL, descriptor);

        String expectedMessage = String.format(UnavailableCommand.MESSAGE_EDIT_APPAREL_SUCCESS, editedApparel);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredApparelList().get(0), editedApparel);
        expectedModel.commitAddressBook();

        assertCommandSuccess(unavailableCommand, model, commandHistory, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredApparelList().size());
        Apparel lastApparel = model.getFilteredApparelList().get(indexLastPerson.getZeroBased());

        ApparelBuilder personInList = new ApparelBuilder(lastApparel);
        Apparel editedApparel = personInList.withName(VALID_NAME_B).withColor(VALID_COLOR_BLUE)
                .build();
        editedApparel.use();
        editedApparel.dirty();

        UnavailablePersonDescriptor descriptor = new UnavailableApparelDescriptorBuilder().withName(VALID_NAME_B)
                .withColor(VALID_COLOR_BLUE).build();
        UnavailableCommand unavailableCommand = new UnavailableCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(UnavailableCommand.MESSAGE_EDIT_APPAREL_SUCCESS, editedApparel);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastApparel, editedApparel);
        expectedModel.commitAddressBook();

        assertCommandSuccess(unavailableCommand, model, commandHistory, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showApparelAtIndex(model, INDEX_FIRST_APPAREL);

        Apparel apparelInFilteredList = model.getFilteredApparelList().get(INDEX_FIRST_APPAREL.getZeroBased());
        Apparel editedApparel = new ApparelBuilder(apparelInFilteredList).withName(VALID_NAME_B).build();
        editedApparel.use();
        editedApparel.dirty();
        UnavailableCommand unavailableCommand = new UnavailableCommand(INDEX_FIRST_APPAREL,
                new UnavailableApparelDescriptorBuilder().withName(VALID_NAME_B).build());

        String expectedMessage = String.format(UnavailableCommand.MESSAGE_EDIT_APPAREL_SUCCESS, editedApparel);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredApparelList().get(0), editedApparel);
        expectedModel.commitAddressBook();

        assertCommandSuccess(unavailableCommand, model, commandHistory, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Apparel firstApparel = model.getFilteredApparelList().get(INDEX_FIRST_APPAREL.getZeroBased());
        UnavailablePersonDescriptor descriptor = new UnavailableApparelDescriptorBuilder(firstApparel).build();
        UnavailableCommand unavailableCommand = new UnavailableCommand(INDEX_SECOND_APPAREL, descriptor);

        assertCommandFailure(unavailableCommand, model, commandHistory, EditCommand.MESSAGE_DUPLICATE_APPAREL);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showApparelAtIndex(model, INDEX_FIRST_APPAREL);

        // edit apparel in filtered list into a duplicate in address book
        Apparel apparelInList = model.getAddressBook().getApparelList().get(INDEX_SECOND_APPAREL.getZeroBased());
        UnavailableCommand unavailableCommand = new UnavailableCommand(INDEX_FIRST_APPAREL,
                new UnavailableApparelDescriptorBuilder(apparelInList).build());

        assertCommandFailure(unavailableCommand, model, commandHistory, EditCommand.MESSAGE_DUPLICATE_APPAREL);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredApparelList().size() + 1);
        UnavailablePersonDescriptor descriptor = new
                UnavailableApparelDescriptorBuilder().withName(VALID_NAME_B).build();
        UnavailableCommand unavailableCommand = new UnavailableCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(unavailableCommand, model, commandHistory,
                Messages.MESSAGE_INVALID_APPAREL_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showApparelAtIndex(model, INDEX_FIRST_APPAREL);
        Index outOfBoundIndex = INDEX_SECOND_APPAREL;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getApparelList().size());

        UnavailableCommand unavailableCommand = new UnavailableCommand(outOfBoundIndex,
                                new UnavailableApparelDescriptorBuilder().withName(VALID_NAME_B).build());

        assertCommandFailure(unavailableCommand, model, commandHistory,
                                Messages.MESSAGE_INVALID_APPAREL_DISPLAYED_INDEX);
    }

    @Test
    public void executeUndoRedo_validIndexUnfilteredList_success() throws Exception {
        Apparel editedApparel = new ApparelBuilder().build();
        Apparel apparelToEdit = model.getFilteredApparelList().get(INDEX_FIRST_APPAREL.getZeroBased());
        UnavailablePersonDescriptor descriptor = new UnavailableApparelDescriptorBuilder(editedApparel).build();
        UnavailableCommand unavailableCommand = new UnavailableCommand(INDEX_FIRST_APPAREL, descriptor);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(apparelToEdit, editedApparel);
        expectedModel.commitAddressBook();

        // edit -> first apparel edited
        unavailableCommand.execute(model, commandHistory);

        // undo -> reverts addressbook back to previous state and filtered apparel list to show all persons
        expectedModel.undoAddressBook();
        assertCommandSuccess(new UndoCommand(), model, commandHistory, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        // redo -> same first apparel edited again
        expectedModel.redoAddressBook();
        assertCommandSuccess(new RedoCommand(), model, commandHistory, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void executeUndoRedo_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredApparelList().size() + 1);
        UnavailablePersonDescriptor descriptor = new UnavailableApparelDescriptorBuilder()
                .withName(VALID_NAME_B).build();
        UnavailableCommand unavailableCommand = new UnavailableCommand(outOfBoundIndex, descriptor);

        // execution failed -> address book state not added into model
        assertCommandFailure(unavailableCommand, model, commandHistory,
                Messages.MESSAGE_INVALID_APPAREL_DISPLAYED_INDEX);

        // single address book state in model -> undoCommand and redoCommand fail
        assertCommandFailure(new UndoCommand(), model, commandHistory, UndoCommand.MESSAGE_FAILURE);
        assertCommandFailure(new RedoCommand(), model, commandHistory, RedoCommand.MESSAGE_FAILURE);
    }

    /**
     * 1. Edits a {@code Apparel} from a filtered list.
     * 2. Undo the edit.
     * 3. The unfiltered list should be shown now. Verify that the index of the previously edited apparel in the
     * unfiltered list is different from the index at the filtered list.
     * 4. Redo the edit. This ensures {@code RedoCommand} edits the apparel object regardless of indexing.
     */
    @Test
    public void executeUndoRedo_validIndexFilteredList_samePersonEdited() throws Exception {
        Apparel editedApparel = new ApparelBuilder().build();
        UnavailablePersonDescriptor descriptor = new UnavailableApparelDescriptorBuilder(editedApparel).build();
        UnavailableCommand unavailableCommand = new UnavailableCommand(INDEX_FIRST_APPAREL, descriptor);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        showApparelAtIndex(model, INDEX_SECOND_APPAREL);
        Apparel apparelToEdit = model.getFilteredApparelList().get(INDEX_FIRST_APPAREL.getZeroBased());
        expectedModel.setPerson(apparelToEdit, editedApparel);
        expectedModel.commitAddressBook();

        // edit -> edits second apparel in unfiltered apparel list / first apparel in filtered apparel list
        unavailableCommand.execute(model, commandHistory);

        // undo -> reverts addressbook back to previous state and filtered apparel list to show all persons
        expectedModel.undoAddressBook();
        assertCommandSuccess(new UndoCommand(), model, commandHistory, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        assertNotEquals(model.getFilteredApparelList().get(INDEX_FIRST_APPAREL.getZeroBased()), apparelToEdit);
        // redo -> edits same second apparel in unfiltered apparel list
        expectedModel.redoAddressBook();
        assertCommandSuccess(new RedoCommand(), model, commandHistory, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void equals() {
        final UnavailableCommand standardCommand = new UnavailableCommand(INDEX_FIRST_APPAREL, DESC_CONNY);

        // same values -> returns true
        UnavailablePersonDescriptor copyDescriptor = new UnavailablePersonDescriptor(DESC_CONNY);
        UnavailableCommand commandWithSameValues = new UnavailableCommand(INDEX_FIRST_APPAREL, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new UnavailableCommand(INDEX_SECOND_APPAREL, DESC_CONNY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new UnavailableCommand(INDEX_FIRST_APPAREL, DESC_DENDI)));
    }

}
