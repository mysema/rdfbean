GET SINGLE LITERAL

<#list resource.properties as property>
  ${property.id} : ${property.literal.value}    
</#list>

GET ALL LITERALS

<#list resource.properties as property>
<#list property.literals as literal>
  ${property.id} : ${literal.value}
</#list>    
</#list>

HOMEPAGE

<#list resource.getProperty(uid).literals as literal>
  ${resource.getProperty(uid).id} : ${literal.value}
</#list>

REFERENCES

<#--list resource.getProperty(uid).references as reference>
  ${reference}
</#list-->