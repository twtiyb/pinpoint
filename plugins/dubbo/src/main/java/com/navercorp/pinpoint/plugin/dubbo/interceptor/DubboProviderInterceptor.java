package com.navercorp.pinpoint.plugin.dubbo.interceptor;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.dubbo.DubboConstants;
import com.navercorp.pinpoint.plugin.dubbo.DubboProviderMethodDescriptor;

import java.io.IOException;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 * @author lanzuyou
 * @author jaehong.kim
 */
public class DubboProviderInterceptor extends SpanRecursiveAroundInterceptor {
	private static final String SCOPE_NAME = "##DUBBO_PROVIDER_TRACE";
	private static final MethodDescriptor DUBBO_PROVIDER_METHOD_DESCRIPTOR = new DubboProviderMethodDescriptor();

	public DubboProviderInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
		super(traceContext, descriptor, SCOPE_NAME);
		traceContext.cacheApi(DUBBO_PROVIDER_METHOD_DESCRIPTOR);
	}

	protected Trace createTrace(Object target, Object[] args) {
		final Trace trace = readRequestTrace(target, args);
		if (trace.canSampled()) {
			final SpanRecorder recorder = trace.getSpanRecorder();
			// You have to record a service type within Server range.
			recorder.recordServiceType(DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
			recorder.recordApi(DUBBO_PROVIDER_METHOD_DESCRIPTOR);
			recordRequest(recorder, target, args);
			setAttachment((RpcInvocation) args[0], "ppId", trace.getTraceId().getTransactionId());
		}

		return trace;
	}

	private void setAttachment(RpcInvocation invocation, String name, String value) {
		invocation.setAttachment(name, value);
		if (isDebug) {
			logger.debug("Set attachment {}={}", name, value);
		}
	}

	private Trace readRequestTrace(Object target, Object[] args) {
		final Invoker invoker = (Invoker) target;
		// Ignore monitor service.
		if (DubboConstants.MONITOR_SERVICE_FQCN.equals(invoker.getInterface().getName())) {
			return traceContext.disableSampling();
		}

		final RpcInvocation invocation = (RpcInvocation) args[0];
		// If this transaction is not traceable, mark as disabled.
		if (invocation.getAttachment(DubboConstants.META_DO_NOT_TRACE) != null) {
			return traceContext.disableSampling();
		}
		final String transactionId = invocation.getAttachment(DubboConstants.META_TRANSACTION_ID);
		// If there's no trasanction id, a new trasaction begins here.
		// FIXME There seems to be cases where the invoke method is called after a span is already created.
		// We'll have to check if a trace object already exists and create a span event instead of a span in that case.
		if (transactionId == null) {
			return traceContext.newTraceObject();
		}

		// otherwise, continue tracing with given data.
		final long parentSpanID = NumberUtils.parseLong(invocation.getAttachment(DubboConstants.META_PARENT_SPAN_ID), SpanId.NULL);
		final long spanID = NumberUtils.parseLong(invocation.getAttachment(DubboConstants.META_SPAN_ID), SpanId.NULL);
		final short flags = NumberUtils.parseShort(invocation.getAttachment(DubboConstants.META_FLAGS), (short) 0);
		final TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);

		return traceContext.continueTraceObject(traceId);
	}

	private void recordRequest(SpanRecorder recorder, Object target, Object[] args) {
		final RpcInvocation invocation = (RpcInvocation) args[0];
		final RpcContext rpcContext = RpcContext.getContext();

		// Record rpc name, client address, server address.
		recorder.recordRpcName(invocation.getInvoker().getInterface().getSimpleName() + ":" + invocation.getMethodName());
		recorder.recordEndPoint(rpcContext.getLocalAddressString());
		if (rpcContext.getRemoteHost() != null) {
			recorder.recordRemoteAddress(rpcContext.getRemoteAddressString());
		} else {
			recorder.recordRemoteAddress("Unknown");
		}

		// If this transaction did not begin here, record parent(client who sent this request) information
		if (!recorder.isRoot()) {
			final String parentApplicationName = invocation.getAttachment(DubboConstants.META_PARENT_APPLICATION_NAME);
			if (parentApplicationName != null) {
				final short parentApplicationType = NumberUtils.parseShort(invocation.getAttachment(DubboConstants.META_PARENT_APPLICATION_TYPE), ServiceType.UNDEFINED.getCode());
				recorder.recordParentApplication(parentApplicationName, parentApplicationType);
				// Pinpoint finds caller - callee relation by matching caller's end point and callee's acceptor host.
				// https://github.com/naver/pinpoint/issues/1395
				recorder.recordAcceptorHost(rpcContext.getLocalAddressString());
			}
		}
	}

	@Override
	protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
		final RpcInvocation invocation = (RpcInvocation) args[0];
		recorder.recordServiceType(DubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
		recorder.recordApi(methodDescriptor);
		recorder.recordAttribute(DubboConstants.DUBBO_RPC_ANNOTATION_KEY,
				invocation.getInvoker().getInterface().getSimpleName() + ":" + invocation.getMethodName());
		try {
			recorder.recordAttribute(DubboConstants.DUBBO_ARGS_ANNOTATION_KEY, JSON.json(invocation.getArguments()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
		final RpcInvocation invocation = (RpcInvocation) args[0];
		recorder.recordApi(methodDescriptor);
		if (throwable == null) {
			try {
				recorder.recordAttribute(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY, JSON.json(((RpcResult)result).getValue()));
			} catch (IOException e) {
				recorder.recordAttribute(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY, "json covered error");
			}
		} else {
			recorder.recordException(throwable);
		}
	}


}