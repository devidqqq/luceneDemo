


import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


/**
 * 创建lucene检索分以下4步
 * 1).通过添加字段（Fields）创建一个Documents
 * 2).创建IndexWriter,并添加创建的Documents
 * 3).使用 QueryParser.parse() 通过需要检索的字符串创建一个query对象
 * 4).创建IndexSearcher，将上一步创建的query传递给他的search()方法
 * @author Du
 * @Date 2018年3月2日
 */
public class LuceneDemo{

	private  Directory dir = null;
	/**
	 * 获得IndexWriter实例
	 * @return IndexWriter
	 * @throws Exception 
	 */
	public IndexWriter creatIndexWroter() throws Exception {
		//在磁盘中创建索引
		dir = FSDirectory.open(Paths.get("c://lucene"));
		//在内存中创建索引
		//Directory directory = new RAMDirectory();
		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();//中文分词
		//实例化IndexWriter
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    IndexWriter iwriter = new IndexWriter(dir, config);
	    return iwriter;
	   
	}
	
	/**
	 * 创建分词索引
	 * @return void
	 * @param blog
	 * @throws Exception 
	 */
	public void addIndex(String string) throws Exception {
		IndexWriter iwriter=creatIndexWroter();
		Document doc = new Document();
	    /*String text = "This is the text to be indexed.";
	    doc.add(new Field("fieldname", text, TextField.TYPE_STORED));*/
	    doc.add(new StringField("FiledName",string,Field.Store.YES));//StringField 不拆分传入的string
	    //拆分传入的string为单个词汇
		//doc.add(new TextField("FiledName",string,Field.Store.YES));
	    iwriter.addDocument(doc);
	    iwriter.close();
	}
	
	/**
	 * 删除指定博客的索引
	 * @param blogId
	 * @throws Exception
	 */
	public void deleteIndex(String string)throws Exception{
		IndexWriter writer=creatIndexWroter();
		writer.deleteDocuments(new Term("FiledName",string));//删除包含filedname和string的document索引文档,可以传入Term数组
		//删除所有document
		//writer.deleteAll();
		//删除包含queries的document索引文档,可以传入Query数组
		//writer.deleteDocuments(Query queries);
		writer.forceMergeDeletes(); // 强制删除
		writer.commit();
		writer.close();
	}

	/**
	 * 匹配查询
	 * @param query
	 * @return
	 * @throws Exception List<Blog>
	 */
	public List<Blog> search(String query) throws Exception{
		dir = FSDirectory.open(Paths.get("c://lucene"));
		DirectoryReader ireader = DirectoryReader.open(dir);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		BooleanQuery.Builder booleanQuery=new BooleanQuery.Builder();//多条件匹配，合并多个query
		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();//中文分词
		//从标题中匹配
		QueryParser parser = new QueryParser("title", analyzer);
		Query query1 = parser.parse(query);
		//从内容中匹配
		QueryParser parser2 = new QueryParser("content", analyzer);
		Query query2 = parser2.parse(query);
		
		booleanQuery.add(query1, BooleanClause.Occur.SHOULD);//BooleanClause.Occur有4和属性 FILTER，MUST，MUST_NOT，SHOULD
		booleanQuery.add(query2, BooleanClause.Occur.SHOULD);
		ScoreDoc[] hits = isearcher.search(booleanQuery.build(), 100).scoreDocs;
		//相关词高亮显示
		QueryScorer scorer=new QueryScorer(query1);
		Fragmenter fragmenter=new SimpleSpanFragmenter(scorer);
		SimpleHTMLFormatter simpleHTMLFormatter=new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
		Highlighter highlighter=new Highlighter(simpleHTMLFormatter, scorer);
		highlighter.setTextFragmenter(fragmenter);


		List<Blog> blogList=new LinkedList<Blog>();
		for(ScoreDoc scoreDoc:hits){
			Document doc=isearcher.doc(scoreDoc.doc);
			String s = doc.get('');
		}
	}
}