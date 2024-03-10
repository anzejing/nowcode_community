package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.stream.IntStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private  static final String REPLACEMENT="***";


    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        try(
                InputStream is= this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ){
            String keyWord;
            while((keyWord = reader.readLine())!=null){
                //添加到前缀树
                this.addKeyWord(keyWord);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //添加一个敏感词到前缀树
    private void addKeyWord(String keyWord){
        TrieNode tempNode = rootNode;
        for (int i=0;i<keyWord.length();i++){
            char c = keyWord.charAt(i);
            TrieNode subNode =  tempNode.getSubNode(c);
            if(subNode==null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            tempNode=subNode;
            if(i==keyWord.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb =new StringBuilder();
        while (position<text.length()){
            char c = text.charAt(position);

            //跳过符号
            if(isSymbol(c)){
                //指针1处于根节点,将此符号计入结果，指针2++
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                sb.append(text.charAt(begin));
                position= ++begin;
                tempNode=rootNode;
            }else if(tempNode.isKeywordEnd()){
                //发现敏感词,将begin到position字符串替换
                sb.append(REPLACEMENT);
                //进入下一个位置
                ++position;
                begin=position;
                tempNode=rootNode;
            }else {
                //检查下一个字符
                position++;
            }

        }
        //记录最后的字符
        sb.append(text.substring(begin));
        return sb.toString();

    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点 key，value是下级字符和节点
        private Map<Character,TrieNode> subNodes = new HashMap<>();



        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }

}
