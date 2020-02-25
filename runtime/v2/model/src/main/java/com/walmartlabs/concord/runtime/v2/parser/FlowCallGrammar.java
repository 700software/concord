package com.walmartlabs.concord.runtime.v2.parser;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2019 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.concord.runtime.v2.model.FlowCall;
import com.walmartlabs.concord.runtime.v2.model.FlowCallOptions;
import com.walmartlabs.concord.runtime.v2.model.ImmutableFlowCallOptions;
import com.walmartlabs.concord.runtime.v2.model.WithItems;
import io.takari.parc.Parser;

import static com.walmartlabs.concord.runtime.v2.parser.CommonGrammar.retryVal;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarMisc.satisfyField;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarMisc.with;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarOptions.optional;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarOptions.options;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarV2.*;

public final class FlowCallGrammar {

    private static final Parser<Atom, FlowCallOptions> callOptions =
            with(FlowCallOptions::builder,
                    o -> options(
                            optional("in", mapVal.map(o::input)),
                            optional("out", stringVal.map(o::out)),
                            optional("meta", mapVal.map(o::meta)),
                            optional("withItems", nonNullVal.map(v -> o.withItems(WithItems.of(v)))),
                            optional("retry", retryVal.map(o::retry))
                    ))
                    .map(ImmutableFlowCallOptions.Builder::build);

    public static final Parser<Atom, FlowCall> callFull =
            satisfyField("call", YamlValueType.FLOW_CALL, a ->
                    stringVal.bind(flowName ->
                            callOptions.map(options -> new FlowCall(a.location, flowName, options))));

    private FlowCallGrammar() {
    }
}