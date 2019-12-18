package com.example.guestbook;

import com.google.appengine.api.NamespaceManager;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

/**
 * An example App Engine namespace setting filter.  
 * 
 * <p>This namespace filter provides for a number of strategies
 * as an example but is also careful not to override the
 * namespace where it has previously been set, for example, incoming 
 * task queue requests.
 */
@WebFilter(filterName = "NamespaceFilter",
           urlPatterns = "/*",
           initParams = @WebInitParam(name = "namespace-strategy", value = "EMPTY")
          )
public class NamespaceFilter implements Filter {
  
  /**
   * Enumeration of namespace strategies.
   */
  enum NamespaceStrategy {
    /**
     * Use the server name shown in the http request as the 
     * namespace name.
     */
    SERVER_NAME,
    
    /**
     * Use the Google Apps domain that was used to direct this
     * URL.
     */
    GOOGLE_APPS_DOMAIN,
    
    /**
     * Use empty namespace.
     */
    EMPTY
  }
  
  // The strategy to use for this instance of the filter.
  private NamespaceStrategy strategy = NamespaceStrategy.SERVER_NAME;
  
  // The filter config.
  private FilterConfig filterConfig;

  private static ThreadLocal<HttpServletRequest> localRequest = new ThreadLocal<HttpServletRequest>();
  private static ThreadLocal<String> currentNamespace = new ThreadLocal<String>();

  public static HttpServletRequest getRequest() {
      return localRequest.get();
  }

  public static HttpSession getSession() {
      HttpServletRequest request = localRequest.get();
      return (request != null) ? request.getSession(false) : null;
  }

  public static String getNamespace() {
    return currentNamespace.get();
  }

  /* @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    this.filterConfig = config;
    String namespaceStrategy = config.getInitParameter("namespace-strategy");
    if (namespaceStrategy != null) {
      try {
        strategy = NamespaceStrategy.valueOf(namespaceStrategy);
      } catch (IllegalArgumentException exception) {
        // Badly configured namespace-strategy
        filterConfig.getServletContext().log(
            "web.xml filter config \"namespace-strategy\" setting " +
            "to \"" + namespaceStrategy + "\" is invalid. Select " +
            "from one of : " + 
            Arrays.asList(NamespaceStrategy.values()).toString());
        throw new ServletException(exception);
      }
    }
  }
  
  /* @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    this.filterConfig = null;
  }

  /* @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, 
   *     javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    // If the NamespaceManager state is already set up from the
    // context of the task creator the current namespace will not
    // be null.  It's important to check that the current namespace
    // has not been set before setting it for this request.
      switch (strategy) {
        case SERVER_NAME : {
          NamespaceManager.set(request.getServerName());
          break;
        }
        
        case GOOGLE_APPS_DOMAIN : {
          NamespaceManager.set(NamespaceManager.getGoogleAppsNamespace());
          break;
        }

        case EMPTY : {
          NamespaceManager.set("");
        }
      }

      currentNamespace.set(NamespaceManager.get());

      if (request instanceof HttpServletRequest) {
        localRequest.set((HttpServletRequest) request);
      }
    
    // chain into the next request
    try {
      chain.doFilter(request, response) ;
    } finally {
      localRequest.remove();
      currentNamespace.remove();
    }
  }
}

