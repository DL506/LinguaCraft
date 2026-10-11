import{Bt as e,Cn as t,F as n,Ft as r,Ht as i,K as a,Lt as o,Mn as s,P as c,Pt as l,Qn as u,Tn as d,Un as f,Vt as p,Y as m,an as h,dn as g,et as _,hn as v,jn as y,pn as b,rn as x,rt as S,sn as C,xn as w}from"./light-TBGKFU-e.js";import{B as T,H as E,S as D,T as O,k}from"./request-BS4zoC18.js";import{n as A}from"./Tag-C-cXGVZa.js";import{t as j}from"./light-Cra7xMlk.js";var M=D&&`loading`in document.createElement(`img`);function N(e={}){let{root:t=null}=e;return{hash:`${e.rootMargin||`0px 0px 0px 0px`}-${Array.isArray(e.threshold)?e.threshold.join(`,`):e.threshold??`0`}`,options:{...e,root:(typeof t==`string`?document.querySelector(t):t)||document.documentElement}}}var P=new WeakMap,F=new WeakMap,I=new WeakMap,L=(e,t,n)=>{if(!e)return()=>{};let r=N(t),{root:i}=r.options,a,o=P.get(i);o?a=o:(a=new Map,P.set(i,a));let s,c;a.has(r.hash)?(c=a.get(r.hash),c[1].has(e)||(s=c[0],c[1].add(e),s.observe(e))):(s=new IntersectionObserver(e=>{e.forEach(e=>{if(e.isIntersecting){let t=F.get(e.target),n=I.get(e.target);t&&t(),n&&(n.value=!0)}})},r.options),s.observe(e),c=[s,new Set([e])],a.set(r.hash,c));let l=!1,u=()=>{l||(F.delete(e),I.delete(e),l=!0,c[1].has(e)&&(c[0].unobserve(e),c[1].delete(e)),c[1].size<=0&&a.delete(r.hash),a.size||P.delete(i))};return F.set(e,u),I.set(e,n),u},R=S(`n-avatar-group`),z=r(`avatar`,`
 width: var(--n-merged-size);
 height: var(--n-merged-size);
 color: #FFF;
 font-size: var(--n-font-size);
 display: inline-flex;
 position: relative;
 overflow: hidden;
 text-align: center;
 border: var(--n-border);
 border-radius: var(--n-border-radius);
 --n-merged-color: var(--n-color);
 background-color: var(--n-merged-color);
 transition:
 border-color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
`,[p(l(`&`,`--n-merged-color: var(--n-color-modal);`)),i(l(`&`,`--n-merged-color: var(--n-color-popover);`)),l(`img`,`
 width: 100%;
 height: 100%;
 `),o(`text`,`
 white-space: nowrap;
 display: inline-block;
 position: absolute;
 left: 50%;
 top: 50%;
 `),r(`icon`,`
 vertical-align: bottom;
 font-size: calc(var(--n-merged-size) - 6px);
 `),o(`text`,`line-height: 1.25`)]),B=[`src`],V={...c.props,size:[String,Number],src:String,circle:{type:Boolean,default:void 0},objectFit:String,round:{type:Boolean,default:void 0},bordered:{type:Boolean,default:void 0},onError:Function,fallbackSrc:String,intersectionObserverOptions:Object,lazy:Boolean,onLoad:Function,renderPlaceholder:Function,renderFallback:Function,imgProps:Object,color:String},H=g({name:`Avatar`,props:V,slots:Object,setup(r){let{mergedClsPrefixRef:i,inlineThemeDisabled:a}=_(r),o=f(!1),l=null,u=f(null),d=f(null),p=()=>{let{value:e}=u;if(e&&(l===null||l!==e.innerHTML)){l=e.innerHTML;let{value:t}=d;if(t){let{offsetWidth:n,offsetHeight:r}=t,{offsetWidth:i,offsetHeight:a}=e,o=.9,s=Math.min(n/i*o,r/a*o,1);e.style.transform=`translateX(-50%) translateY(-50%) scale(${s})`}}},m=v(R,null),h=x(()=>{let{size:e}=r;if(e)return e;let{size:t}=m||{};return t||`medium`}),g=c(`Avatar`,`-avatar`,z,j,r,i),b=v(A,null),S=x(()=>{if(m)return!0;let{round:e,circle:t}=r;return e!==void 0||t!==void 0?e||t:b?b.roundRef.value:!1}),C=x(()=>m?!0:r.bordered||!1),T=x(()=>{let t=h.value,n=S.value,i=C.value,{color:a}=r,{self:{borderRadius:o,fontSize:s,color:c,border:l,colorModal:u,colorPopover:d},common:{cubicBezierEaseInOut:f}}=g.value,p;return p=typeof t==`number`?`${t}px`:g.value.self[e(`height`,t)],{"--n-font-size":s,"--n-border":i?l:`none`,"--n-border-radius":n?`50%`:o,"--n-color":a||c,"--n-color-modal":a||u,"--n-color-popover":a||d,"--n-bezier":f,"--n-merged-size":`var(--n-avatar-size-override, ${p})`}}),E=a?n(`avatar`,x(()=>{let e=h.value,t=S.value,n=C.value,{color:i}=r,a=``;return e&&(a+=typeof e==`number`?`a${e}`:e[0]),t&&(a+=`b`),n&&(a+=`c`),i&&(a+=O(i)),a}),T,r):void 0,D=f(!r.lazy);t(()=>{if(r.lazy&&r.intersectionObserverOptions){let e,t=s(()=>{e?.(),e=void 0,r.lazy&&(e=L(d.value,r.intersectionObserverOptions,D))});w(()=>{t(),e?.()})}}),y(()=>r.src||r.imgProps?.src,()=>{o.value=!1});let k=f(!r.lazy);return{textRef:u,selfRef:d,mergedRoundRef:S,mergedClsPrefix:i,fitTextTransform:p,cssVars:a?void 0:T,themeClass:E?.themeClass,onRender:E?.onRender,hasLoadError:o,shouldStartLoading:D,loaded:k,mergedOnError:e=>{if(!D.value)return;o.value=!0;let{onError:t,imgProps:{onError:n}={}}=r;t?.(e),n?.(e)},mergedOnLoad:e=>{let{onLoad:t,imgProps:{onLoad:n}={}}=r;t?.(e),n?.(e),k.value=!0}}},render(){let{$slots:e,src:t,mergedClsPrefix:n,lazy:r,onRender:i,loaded:o,hasLoadError:s,imgProps:c={}}=this;i?.();let l,f=!o&&!s&&(this.renderPlaceholder?this.renderPlaceholder():this.$slots.placeholder?.());return l=this.hasLoadError?this.renderFallback?this.renderFallback():T(e.fallback,()=>[(d(),C(`img`,{src:this.fallbackSrc,style:u({objectFit:this.objectFit})},null,12,B))]):E(e.default,e=>{if(e)return d(),h(k,{key:1,onResize:this.fitTextTransform},{default:()=>(d(),C(`span`,{ref:`textRef`,class:a(`${n}-avatar__text`)},[m(()=>e)],2))},1032,[`onResize`]);if(t||c.src){let e=this.src||c.src;return b(`img`,{...c,loading:M&&!this.intersectionObserverOptions&&r?`lazy`:`eager`,src:r&&this.intersectionObserverOptions?this.shouldStartLoading?e:void 0:e,"data-image-src":e,onLoad:this.mergedOnLoad,onError:this.mergedOnError,style:[c.style||``,{objectFit:this.objectFit},f?{height:`0`,width:`0`,visibility:`hidden`,position:`absolute`}:``]})}}),d(),C(`span`,{ref:`selfRef`,class:a([`${n}-avatar`,this.themeClass]),style:u(this.cssVars)},[m(()=>l),m(()=>r&&f)],6)}});export{H as t};