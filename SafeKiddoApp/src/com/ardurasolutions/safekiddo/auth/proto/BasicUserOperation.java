/**
 * 
 */
package com.ardurasolutions.safekiddo.auth.proto;


public abstract class BasicUserOperation {
	
	public static interface OnError {
		public void onError(int errorCode, Object extraData);
	}
	
	private OnError mOnError;
	
	/**
	 * 
	 * @return 0 if success else non zero
	 */
	public abstract int execute();
	public abstract Object getErrorExtra();

	public OnError getOnError() {
		return mOnError;
	}

	public BasicUserOperation setOnError(OnError mOnError) {
		this.mOnError = mOnError;
		return this;
	}

}
