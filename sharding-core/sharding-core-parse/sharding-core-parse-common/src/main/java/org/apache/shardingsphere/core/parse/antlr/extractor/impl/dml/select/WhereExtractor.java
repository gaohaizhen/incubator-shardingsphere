/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.WhereSegment;

import java.util.Map;

/**
 * Where extractor.
 *
 * @author duhongjun
 */
public final class WhereExtractor extends AbstractWhereExtractor {
    
    @Override
    protected Optional<ParserRuleContext> extractWhere(final WhereSegment whereSegment, final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> placeholderIndexes) {
        Optional<ParserRuleContext> selectItemsNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_ITEMS);
        if (!selectItemsNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> fromNode = ExtractorUtils.findFirstChildNodeNoneRecursive(selectItemsNode.get().getParent(), RuleName.FROM_CLAUSE);
        if (!fromNode.isPresent()) {
            return Optional.absent();
        }
        return ExtractorUtils.findFirstChildNodeNoneRecursive(fromNode.get().getParent(), RuleName.WHERE_CLAUSE);
    }
}
