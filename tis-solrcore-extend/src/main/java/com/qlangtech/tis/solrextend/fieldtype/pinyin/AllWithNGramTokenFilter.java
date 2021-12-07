/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrextend.fieldtype.pinyin;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.CodepointCountFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.CharacterUtils;

/**
 * 为了能够在N元分词时候，保留一个源词的最长分词作为查询条件
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AllWithNGramTokenFilter extends TokenFilter {

    // public static final int DEFAULT_MIN_NGRAM_SIZE = 1;
    // public static final int DEFAULT_MAX_NGRAM_SIZE = 2;
    private final int minGram, maxGram;

    private char[] curTermBuffer;

    private int curTermLength;

    private int curCodePointCount;

    private int curGramSize;

    private int curPos;

    // private int curPosInc, curPosLen;
    private int tokStart;

    private int tokEnd;

    // only if the length changed before this
    private boolean hasIllegalOffsets;

    // filter
    // private final CharacterUtils charUtils;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public AllWithNGramTokenFilter(TokenStream input, int minGram, int maxGram) {
        super(new CodepointCountFilter(input, minGram, Integer.MAX_VALUE));
        // this.charUtils = CharacterUtils.getInstance();
        if (minGram < 1) {
            throw new IllegalArgumentException("minGram must be greater than zero");
        }
        if (minGram > maxGram) {
            throw new IllegalArgumentException("minGram must not be greater than maxGram");
        }
        this.minGram = minGram;
        this.maxGram = maxGram;
    }

    /**
     * Returns the next token in the stream, or null at EOS.
     */
    @Override
    public final boolean incrementToken() throws IOException {
        while (true) {
            if (curTermBuffer == null) {
                if (!input.incrementToken()) {
                    return false;
                } else {
                    curTermBuffer = termAtt.buffer().clone();
                    curTermLength = termAtt.length();
                    curCodePointCount = Character.codePointCount(termAtt, 0, termAtt.length());
                    curGramSize = minGram;
                    curPos = 0;
                    this.appendAttribute(curTermBuffer, curTermLength, 0, curTermLength);
                    // curPosInc = posIncAtt.getPositionIncrement();
                    // curPosLen = posLenAtt.getPositionLength();
                    // tokStart = offsetAtt.startOffset();
                    // tokEnd = offsetAtt.endOffset();
                    // if length by start + end offsets doesn't match the term
                    // text then assume
                    // this is a synonym and don't adjust the offsets.
                    hasIllegalOffsets = (tokStart + curTermLength) != tokEnd;
                    // curTermBuffer.length);
                    return true;
                // 百岁add///////////////////////////////////////////
                }
            }
            if (curGramSize > maxGram || (curPos + curGramSize) > curCodePointCount) {
                ++curPos;
                curGramSize = minGram;
            }
            if ((curPos + curGramSize) <= curCodePointCount) {
                clearAttributes();
                final int start = Character.offsetByCodePoints(curTermBuffer, 0, curTermLength, 0, curPos);
                final int end = Character.offsetByCodePoints(curTermBuffer, 0, curTermLength, start, curGramSize);
                termAtt.copyBuffer(curTermBuffer, start, end - start);
                this.appendAttribute(curTermBuffer, curTermLength, start, end);
                curGramSize++;
                return true;
            }
            curTermBuffer = null;
        }
    }

    protected void appendAttribute(char[] curTermBuffer, int curTermLength, int start, int end) {
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        curTermBuffer = null;
    }
}
