/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidpn.server.service;

/**
 * 
 * @author Pecan 
 * 类说明：
 */
public class InfrastructureException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InfrastructureException() {
    }

    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public InfrastructureException(Throwable cause) {
        super(cause);
    }
}
