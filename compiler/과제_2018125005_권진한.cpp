#include <stdio.h>
#include <string.h>
typedef struct Token
{
    int att;//type의 종류
    char contents[20];//type의 내용
};
char bf;//pushback용 buffer
char cur_line[100]; //현재 줄
int cur_index=0; //현재 줄에서의 index
bool dont_p=false;
Token this_Token;//현재 lexing중인 token
bool is_int(char a){
    if(a>='0'&&a<='9')
        return 1;
    else
        return 0;
}
bool is_char(char a){
    if((a>='A'&&a<='Z')||(a>='a'&&a<='z')||a=='_')
        return 1;
    else
        return 0;
}
int what_type(char a){
    int att;
    switch (a)
    {
    case ' ':
        att=1;
    case '/':
        if(cur_line[++cur_index]=='/'){
            att=2;
            this_Token.contents[1]='/';
        }
        else{
            att=3;
            bf=cur_line[cur_index];
            cur_index--;
        }
        break;
    case '=':
        if(cur_line[++cur_index]=='='){
            this_Token.contents[1]='=';
            att=4;
        }
        else{
            att=5;
            bf=cur_line[cur_index];
            cur_index--;
        }
        break;
    case '+':
        att=5;
        break;
    case '-':
        att=6;
        break;
    case '*':
        att=7;
        break;
    case '&':
        if(cur_line[++cur_index]=='&'){
            this_Token.contents[1]='&';
            att=7;
        }
        break;
    case '|':
        if(cur_line[++cur_index]=='|'){
            this_Token.contents[1]='|';
            att=8;
        }
        break;
    case '!':
        if(cur_line[++cur_index]=='='){
            this_Token.contents[1]='=';
            att=9;
        }
        else{
            att=10;
            bf=cur_line[cur_index];
            cur_index--;
        }
        break;
    case '>':
        if(cur_line[++cur_index]=='='){
            this_Token.contents[1]='=';
            att=11;
        }
        else{
            att=12;
            cur_index--;
            bf=cur_line[cur_index];
        }
        break;    
    case '<':
        if(cur_line[++cur_index]=='='){
            this_Token.contents[1]='=';
            att=13;
        }
        else{
            att=14;
            bf=cur_line[cur_index];
            cur_index--;
        }
        break; 
    case '%':
        att=15;
        break;
    case '(':
        att=16;
        break;
    case ')':
        att=17;
        break;
    case '{':
        att=18;
        break;
    case '}':
        att=19;
        break;
    case ';':
        att=20;
        break;
    case '[':
        att=21;
        break;
    case ']':
        att=22;
        break;
    default:
        att=23;//문자열,숫자,변수이름,if,for
        break;
    }
    return att;
}
void get_string(){
    int s_index=1;
    if(is_char(this_Token.contents[0])){
        while(is_char(cur_line[++cur_index])||is_int(cur_line[cur_index])){
            this_Token.att=23;//Id
            this_Token.contents[s_index]=cur_line[cur_index];
            s_index++;
        }
        if(!strcmp(this_Token.contents,"int")){
            printf("Int\n");
            dont_p=true;
        }
        else if(!strcmp(this_Token.contents,"float")){
            printf("Float\n");
            dont_p=true;
        }
        else if(!strcmp(this_Token.contents,"char")){
            printf("Char\n");
            dont_p=true;
        }
        else if(!strcmp(this_Token.contents,"for")){
            printf("for\n");
            dont_p=true;
        }
        else if(!strcmp(this_Token.contents,"if")){
            printf("if\n");
            dont_p=true;
        }
        else if(!strcmp(this_Token.contents,"else")){
            printf("else\n");
            dont_p=true;
        }
        else{
            printf("Id ");
        }
        bf=cur_line[cur_index];
        cur_index--;
    }
    else if(!strcmp(this_Token.contents,",")){
            dont_p=true;
    }
    else if(is_int(this_Token.contents[0])){
        this_Token.att=24;//IntLiteral
        while(is_int(cur_line[++cur_index])||cur_line[cur_index]=='.'){
            if(cur_line[cur_index]=='.'){
                this_Token.att=25;//FloatLiteral
            }
            this_Token.contents[s_index]=cur_line[cur_index];
            s_index++;
        }
        bf=cur_line[cur_index];
        cur_index--;
        if(this_Token.att==24)
            printf("IntLiteral ");
        else
            printf("FloatLiteral ");
    }
}
Token get_Tocken(){//parser가 next token을 필요로 할 때 사용하는 함수, 입력 파일에 있는 다음 토큰을 반환한다.
    int att;
    if(bf=='\0'||bf==' '){
        this_Token.contents[0]=cur_line[cur_index];
        att=what_type(cur_line[cur_index]);
    }
    else{
        this_Token.contents[0]=bf;
        att=what_type(bf);
        bf='\0';
    }
    this_Token.att=att;
    if(this_Token.att==23){
        get_string();
    }
    return this_Token;
}

int main(){
    Token k;
    int i=1;
    int j=0;
    char cur;
    while((cur=getchar())!=EOF){
        if(cur=='\n'){
            printf("Line %d %s\n",i,cur_line);
            i++;
            while(!(cur_line[cur_index]=='\0'&&bf=='\0')){
                if(cur_line[cur_index]==' '){
                    cur_index++;
                    continue;
                }
                k=get_Tocken();
                cur_index++;
                if(this_Token.att==2){
                    memset(this_Token.contents,0,20);
                    cur_index=100;
                    continue;
                }
                if(dont_p==false){
                    printf("%s\n",k.contents);
                }
                else
                    dont_p=false;
                memset(this_Token.contents,0,20);
            }
            memset(cur_line,0,100);
            j=0;
            cur_index=0;
        }
        else{
            cur_line[j]=cur;
            j++;
        }
    }
}


