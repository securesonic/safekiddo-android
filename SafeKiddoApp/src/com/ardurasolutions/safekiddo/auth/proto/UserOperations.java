package com.ardurasolutions.safekiddo.auth.proto;

import java.util.ArrayList;

import com.hv.console.Console;

public class UserOperations {
	
	public static interface OnSuccessAllOperations {
		public void onSuccessAllOperations();
	}
	
	private ArrayList<BasicUserOperation> operations = new ArrayList<BasicUserOperation>();
	private Thread mThread;
	private OnSuccessAllOperations mOnSuccessAllOperations;
	
	public UserOperations interrupt() {
		if (mThread != null && !mThread.isInterrupted())
			mThread.interrupt();
		return this;
	}
	
	public UserOperations addOperation(BasicUserOperation ba) {
		operations.add(ba);
		return this;
	}
	
	public UserOperations execute() {
		mThread = new Thread() {
			@Override
			public void run() {
				boolean isAllOperationsSuccess = true;
				int errorCode = 0;
				
				if (operations.size() > 0) {
					for(BasicUserOperation operation : operations) {
						errorCode = operation.execute();
						if (errorCode != 0) {
							isAllOperationsSuccess = false;
							if (operation.getOnError() != null)
								operation.getOnError().onError(errorCode, operation.getErrorExtra());
							break;
						}
						
						if (Thread.currentThread().isInterrupted()) {
							if (Console.isEnabled())
								Console.logd("UserOperation interrupted: " + operations.getClass().getName());
							break;
						}
					}
				}
				
				if (isAllOperationsSuccess) {
					if (getOnSuccessAllOperations() != null)
						getOnSuccessAllOperations().onSuccessAllOperations();
				}
				
			}
		};
		mThread.start();
		return this;
	}
	
	public void executeSync() {
		boolean isAllOperationsSuccess = true;
		int errorCode = 0;
		
		if (operations.size() > 0) {
			for(BasicUserOperation operation : operations) {
				errorCode = operation.execute();
				if (errorCode != 0) {
					isAllOperationsSuccess = false;
					if (operation.getOnError() != null)
						operation.getOnError().onError(errorCode, operation.getErrorExtra());
					break;
				}
				
				if (Thread.currentThread().isInterrupted()) {
					if (Console.isEnabled())
						Console.logd("UserOperation interrupted: " + operations.getClass().getName());
					break;
				}
			}
		}
		
		if (isAllOperationsSuccess) {
			if (getOnSuccessAllOperations() != null)
				getOnSuccessAllOperations().onSuccessAllOperations();
		}
	}

	public OnSuccessAllOperations getOnSuccessAllOperations() {
		return mOnSuccessAllOperations;
	}

	public UserOperations setOnSuccessAllOperations(OnSuccessAllOperations mOnSuccessAllOperations) {
		this.mOnSuccessAllOperations = mOnSuccessAllOperations;
		return this;
	}

}
