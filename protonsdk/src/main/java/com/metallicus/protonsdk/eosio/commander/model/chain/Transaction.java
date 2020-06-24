package com.metallicus.protonsdk.eosio.commander.model.chain;

import com.google.gson.annotations.Expose;
import com.metallicus.protonsdk.eosio.commander.model.types.EosType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapnibble on 2018-03-19.
 */

public class Transaction extends TransactionHeader {
	@Expose
	private List<Action> context_free_actions = new ArrayList<>();

	@Expose
	private List<Action> actions = null;

	// Extensions are prefixed with type and are a buffer that can be interpreted by code that is aware and ignored by unaware code.
	@Expose
	private List<String> transaction_extensions = new ArrayList<>();

	public Transaction() {
		super();
	}

	public Transaction(Transaction other) {
		super(other);
		this.context_free_actions = deepCopyOnlyContainer(other.context_free_actions);
		this.actions = deepCopyActions(other.actions);
		this.transaction_extensions = other.transaction_extensions;
	}

	public void addAction(Action msg) {
		if (null == actions) {
			actions = new ArrayList<>(1);
		}

		actions.add(msg);
	}


	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public int getContextFreeActionCount() {
		return (actions == null ? 0 : actions.size());
	}


	<T> List<T> deepCopyOnlyContainer(List<T> srcList) {
		if (null == srcList) {
			return null;
		}

		List<T> newList = new ArrayList<>(srcList.size());
		newList.addAll(srcList);

		return newList;
	}

	// Added by joey-harward on 8/7/19
	private List<Action> deepCopyActions(List<Action> actions) {
		if (null == actions) {
			return null;
		}

		List<Action> newList = new ArrayList<>(actions.size());

		for (Action action : actions) {
			Action actionDeepCopy = new Action(action);
			newList.add(actionDeepCopy);
		}

		return newList;
	}

	@Override
	public void pack(EosType.Writer writer) {
		super.pack(writer);

		writer.putCollection(context_free_actions);
		writer.putCollection(actions);
		//writer.putCollection(transaction_extensions);
		writer.putVariableUInt(transaction_extensions.size());
		if (transaction_extensions.size() > 0) {
			// TODO 구체적 코드가 나오면 확인후 구현할 것.
		}
	}
}
