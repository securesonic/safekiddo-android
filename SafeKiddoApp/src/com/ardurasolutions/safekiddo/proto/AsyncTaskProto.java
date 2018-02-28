package com.ardurasolutions.safekiddo.proto;

import com.ardurasolutions.safekiddo.extra.AsyncTask;

public abstract class AsyncTaskProto<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> { // Params, Progress, Result
	
	public static interface OnUnregisterTask {
		public void onUregister();
	}
	
	private String taskTagName = this.getClass().getName();
	private OnUnregisterTask mOnUnregisterTask;
	private boolean isUregisterCalled = false;
	
	public AsyncTaskProto() { }
	
	public AsyncTaskProto(String tagName) {
		setTaskTagName(tagName);
	}

	@Override
	public void onPreExecute() {
		super.onPreExecute();
		ApplicationProto.get().registerTask(getTaskTagName(), this);
	}
	
	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		ApplicationProto.get().unregisterTask(getTaskTagName());
		if (getOnUnregisterTask() != null && !isUregisterCalled) {
			getOnUnregisterTask().onUregister();
			isUregisterCalled = true;
		}
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		ApplicationProto.get().unregisterTask(getTaskTagName());
		if (getOnUnregisterTask() != null && !isUregisterCalled) {
			getOnUnregisterTask().onUregister();
			isUregisterCalled = true;
		}
	}
	
	@Override
	protected void onCancelled(Result result) {
		super.onCancelled(result);
		ApplicationProto.get().unregisterTask(getTaskTagName());
		if (getOnUnregisterTask() != null && !isUregisterCalled) {
			getOnUnregisterTask().onUregister();
			isUregisterCalled = true;
		}
	}

	public String getTaskTagName() {
		return taskTagName;
	}

	public AsyncTaskProto<Params, Progress, Result> setTaskTagName(String taskTagName) {
		this.taskTagName = taskTagName;
		return this;
	}

	public OnUnregisterTask getOnUnregisterTask() {
		return mOnUnregisterTask;
	}

	public AsyncTaskProto<Params, Progress, Result> setOnUnregisterTask(OnUnregisterTask mOnUnregisterTask) {
		this.mOnUnregisterTask = mOnUnregisterTask;
		return this;
	}

}
